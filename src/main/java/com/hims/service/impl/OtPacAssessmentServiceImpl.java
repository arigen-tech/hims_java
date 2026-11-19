package com.hims.service.impl;

import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.mapper.OtPacAssessmentMapper;
import com.hims.request.OtPacAssessmentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OtPacAssessmentResponse;
import com.hims.service.OtPacAssessmentService;
import com.hims.service.UserContextService;
import com.hims.utils.ResponseUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class OtPacAssessmentServiceImpl implements OtPacAssessmentService {

    private final OtPacAssessmentRepository assessmentRepository;
    private final OtPacMedicalHistoryRepository historyRepository;
    private final OtPacInstructionRepository instructionRepository;
    private final OtPacDocumentRepository documentRepository;
    private final UserContextService userContextService;

    @Value("${upload.pacCopy.path}")
    private String DOCUMENT_FOLDER;

    public OtPacAssessmentServiceImpl(
            OtPacAssessmentRepository assessmentRepository,
            OtPacMedicalHistoryRepository historyRepository,
            OtPacInstructionRepository instructionRepository,
            OtPacDocumentRepository documentRepository,
            UserContextService userContextService) {
        this.assessmentRepository = assessmentRepository;
        this.historyRepository = historyRepository;
        this.instructionRepository = instructionRepository;
        this.documentRepository = documentRepository;
        this.userContextService = userContextService;
    }

    @Override
    @Transactional
    public ApiResponse<OtPacAssessmentResponse> save(
            OtPacAssessmentRequest request,
            List<MultipartFile> files,
            List<String> documentNames) {
        String actor = getCurrentUserFullName();
        LocalDateTime now = LocalDateTime.now();

        OtBooking booking = assessmentRepository.lockBooking(request.getOtBookingId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "OT booking not found"));

        Optional<OtPacAssessment> existing = assessmentRepository
                .findLatestByBookingId(request.getOtBookingId());

        OtPacAssessment assessment;
        if (existing.isPresent()) {
            assessment = updateExisting(existing.get(), request, actor, now);
        } else {
            assessment = createAssessment(booking, request, actor, now);
        }

        saveChildren(assessment, request, files, documentNames, actor, now);
        updateBookingPacStatus(booking, assessment, request);
        return successResponse(loadResponse(assessment));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OtPacAssessmentResponse> getById(Long assessmentId) {
        OtPacAssessment assessment = assessmentRepository.findNativeById(assessmentId)
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "PAC assessment not found"));
        return successResponse(loadResponse(assessment));
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OtPacAssessmentResponse> getByBookingId(Long bookingId) {
        OtPacAssessment assessment = assessmentRepository.findLatestByBookingId(bookingId)
                .orElseThrow(() -> new SDDException(
                        HttpStatus.NOT_FOUND.value(), "PAC assessment not found for booking"));
        return successResponse(loadResponse(assessment));
    }

    private OtPacAssessment createAssessment(
            OtBooking booking,
            OtPacAssessmentRequest request,
            String actor,
            LocalDateTime now) {
        OtPacAssessment assessment = new OtPacAssessment();
        OtPacAssessmentMapper.copyRequest(request, assessment);
        assessment.setOtBooking(booking);
        assessment.setAssessmentNo(assessmentRepository.nextAssessmentNo(booking.getOtBookingId()));
        assessment.setAssessmentDate(now);
        assessment.setLastChgBy(actor);
        assessment.setLastChgDate(now);
        assessment.setStatus("Y");
        assessment.setAnaesthetistId(assessmentRepository.findAnaesthetistId(booking.getOtBookingId())
                .orElse(null));
        applyAssessmentState(request, assessment, now);
        return assessmentRepository.saveAndFlush(assessment);
    }

    private OtPacAssessment updateExisting(
            OtPacAssessment assessment,
            OtPacAssessmentRequest request,
            String actor,
            LocalDateTime now) {
        if (!Objects.equals(assessment.getOtBooking().getOtBookingId(), request.getOtBookingId())) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Booking ID does not match assessment");
        }

        OtPacAssessmentMapper.copyRequest(request, assessment);
        assessment.setLastChgBy(actor);
        assessment.setLastChgDate(now);
        assessment.setAnaesthetistId(assessmentRepository.findAnaesthetistId(request.getOtBookingId())
                .orElse(assessment.getAnaesthetistId()));
        applyAssessmentState(request, assessment, now);

        OtPacAssessment saved = assessmentRepository.saveAndFlush(assessment);
        historyRepository.deleteByPacAssessment_PacAssessmentId(saved.getPacAssessmentId());
        instructionRepository.deleteByPacAssessment_PacAssessmentId(saved.getPacAssessmentId());
        return saved;
    }

    private void applyAssessmentState(
            OtPacAssessmentRequest request,
            OtPacAssessment assessment,
            LocalDateTime now) {
        String submissionStatus = normalize(request.getSubmissionStatus());
        if (!"D".equals(submissionStatus) && !"S".equals(submissionStatus)) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                    "submissionStatus must be D (draft) or S (submit)");
        }

        String clearanceStatus = switch (normalize(request.getClearanceStatus())) {
            case "C", "CLEARED" -> "CLEARED";
            case "P", "PENDING" -> "PENDING";
            case "N", "NOT_CLEARED" -> "NOT_CLEARED";
            default -> throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                    "clearanceStatus must be C, P or N");
        };
        assessment.setClearanceStatus(clearanceStatus);

        if ("S".equals(submissionStatus)) {
            if (assessment.getAnaesthetistId() == null
                    || assessment.getAsaGradeId() == null
                    || assessment.getAnaesthesiaTypeId() == null) {
                throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                        "Anaesthetist, ASA grade and anaesthesia type are required to submit");
            }
            assessment.setAssessmentStatus("FINALIZED");
            assessment.setFinalizedDate(now);
        } else {
            assessment.setAssessmentStatus("DRAFT");
            assessment.setFinalizedDate(null);
        }

        if (assessment.getAsaEmergency() == null) assessment.setAsaEmergency("N");
        if (assessment.getSpecialistReferralRequired() == null) assessment.setSpecialistReferralRequired("N");
        if (assessment.getPreviousAnaesthesia() == null) assessment.setPreviousAnaesthesia("N");
    }

    private void updateBookingPacStatus(
            OtBooking booking,
            OtPacAssessment assessment,
            OtPacAssessmentRequest request) {
        if (!"S".equals(normalize(request.getSubmissionStatus()))) return;

        switch (assessment.getClearanceStatus()) {
            case "CLEARED" -> {
                booking.setIsPacDone("Y");
                booking.setPacStatus("CLEARED");
            }
            case "NOT_CLEARED" -> {
                booking.setIsPacDone("Y");
                booking.setPacStatus("NOT_CLEARED");
            }
            case "PENDING" -> {
                // A pending clearance does not change the booking's PAC state.
            }
            default -> throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                    "Unsupported PAC clearance status");
        }
    }

    private void saveChildren(
            OtPacAssessment assessment,
            OtPacAssessmentRequest request,
            List<MultipartFile> files,
            List<String> documentNames,
            String actor,
            LocalDateTime now) {
        if (request.getMedicalHistory() != null) {
            request.getMedicalHistory().forEach(history -> {
                if (history.getDiseaseName() == null || history.getDiseaseName().isBlank()) {
                    throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Disease name is required");
                }
                historyRepository.save(OtPacMedicalHistory.builder()
                        .pacAssessment(assessment)
                        .diseaseName(history.getDiseaseName().trim())
                        .diseaseStatus(history.getDiseaseStatus())
                        .duration(history.getDuration())
                        .remarks(history.getRemarks())
                        .build());
            });
        }

        if (request.getInstructions() != null) {
            request.getInstructions().stream()
                    .filter(text -> text != null && !text.isBlank())
                    .map(String::trim)
                    .map(text -> OtPacInstruction.builder()
                            .pacAssessment(assessment)
                            .instructionText(text)
                            .lastChgBy(actor)
                            .lastChgDate(now)
                            .build())
                    .forEach(instructionRepository::save);
        }

        saveDocuments(assessment, files, documentNames, actor, now);
    }

    private void saveDocuments(
            OtPacAssessment assessment,
            List<MultipartFile> files,
            List<String> documentNames,
            String actor,
            LocalDateTime now) {
        if (files == null || files.isEmpty()) return;
        if (documentNames == null || documentNames.size() != files.size()) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(),
                    "Provide one document name for each uploaded file");
        }

        Path directory = Paths.get(DOCUMENT_FOLDER).toAbsolutePath().normalize();
        try {
            Files.createDirectories(directory);
            for (int index = 0; index < files.size(); index++) {
                MultipartFile file = files.get(index);
                validateDocument(file);

                String originalName = Paths.get(Objects.requireNonNullElse(file.getOriginalFilename(), "upload"))
                        .getFileName().toString();
                String storedName = UUID.randomUUID() + "_" + originalName;
                Path target = directory.resolve(storedName).normalize();
                if (!target.startsWith(directory)) {
                    throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Invalid filename");
                }

                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                documentRepository.save(OtPacDocument.builder()
                        .pacAssessment(assessment)
                        .documentName(documentNames.get(index))
                        .fileName(storedName)
                        .filePath(target.toString())
                        .mimeType(file.getContentType())
                        .fileSize(file.getSize())
                        .uploadedBy(actor)
                        .uploadedDate(now)
                        .lastChgBy(actor)
                        .lastChgDate(now)
                        .status("Y")
                        .build());
            }
        } catch (IOException e) {
            throw new SDDException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unable to store PAC document");
        }
    }

    private void validateDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Uploaded document cannot be empty");
        }
        String contentType = file.getContentType();
        String normalizedContentType = contentType == null
                ? ""
                : contentType.toLowerCase(Locale.ROOT);
        if (!normalizedContentType.startsWith("image/")
                && !"application/pdf".equals(normalizedContentType)) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Only image and PDF documents are allowed");
        }
    }

    private OtPacAssessmentResponse loadResponse(OtPacAssessment assessment) {
        Long assessmentId = assessment.getPacAssessmentId();
        return OtPacAssessmentMapper.toResponse(
                assessment,
                historyRepository.findNativeByAssessmentId(assessmentId),
                instructionRepository.findNativeByAssessmentId(assessmentId),
                documentRepository.findNativeByAssessmentId(assessmentId));
    }

    private String getCurrentUserFullName() {
        var userContext = userContextService.getCurrentUserContext();
        if (userContext == null || userContext.getUserFullName() == null
                || userContext.getUserFullName().isBlank()) {
            throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "Current user context not found");
        }
        return userContext.getUserFullName();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
    }

    private ApiResponse<OtPacAssessmentResponse> successResponse(OtPacAssessmentResponse response) {
        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
    }
}
