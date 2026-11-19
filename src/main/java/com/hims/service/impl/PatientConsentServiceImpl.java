package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.MasConsentType;
import com.hims.entity.MasSurgery;
import com.hims.entity.OtBooking;
import com.hims.entity.PatientConsent;
import com.hims.entity.repository.MasConsentTypeRepository;
import com.hims.entity.repository.MasSurgeryRepository;
import com.hims.entity.repository.OtBookingRepository;
import com.hims.entity.repository.PatientConsentRepository;
import com.hims.exception.SDDException;
import com.hims.mapper.PatientConsentMapper;
import com.hims.projection.PatientConsentProjection;
import com.hims.request.PatientConsentRequest;
import com.hims.response.ApiResponse;
import com.hims.response.PatientConsentResponse;
import com.hims.response.UserContext;
import com.hims.service.PatientConsentService;
import com.hims.service.UserContextService;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
import java.util.UUID;

@Service
public class PatientConsentServiceImpl implements PatientConsentService {

    private final PatientConsentRepository consentRepository;
    private final MasConsentTypeRepository consentTypeRepository;
    private final OtBookingRepository otBookingRepository;
    private final MasSurgeryRepository surgeryRepository;
    private final PatientConsentMapper mapper;
    private final UserContextService userContextService;

    @Value("$(upload.patientConsent.path)")
    private String CONSENT_DIRECTORY;

    public PatientConsentServiceImpl(PatientConsentRepository consentRepository,
                                     MasConsentTypeRepository consentTypeRepository,
                                     OtBookingRepository otBookingRepository,
                                     MasSurgeryRepository surgeryRepository,
                                     PatientConsentMapper mapper,
                                     UserContextService userContextService) {
        this.consentRepository = consentRepository;
        this.consentTypeRepository = consentTypeRepository;
        this.otBookingRepository = otBookingRepository;
        this.surgeryRepository = surgeryRepository;
        this.mapper = mapper;
        this.userContextService = userContextService;
    }

    @Override
    @Transactional
    public ApiResponse<PatientConsentResponse> upload(PatientConsentRequest request) {
        if (request.getAdmissionId() == null && request.getOtBookingId() == null) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Provide admissionId, otBookingId, or both");
        }
        MultipartFile file = request.getDocument();
        validateDocument(file);
        String originalName = Paths.get(file.getOriginalFilename() == null ? "consent-document" : file.getOriginalFilename())
                .getFileName().toString();
        if (originalName.length() > 255) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Document filename must not exceed 255 characters");
        }

        MasConsentType consentType = consentTypeRepository.findById(request.getConsentTypeId())
                .filter(type -> "Y".equalsIgnoreCase(type.getStatus()))
                .orElseThrow(() -> new SDDException(HttpStatus.BAD_REQUEST.value(), "Active consent type not found"));
        OtBooking booking = request.getOtBookingId() == null ? null : otBookingRepository.findById(request.getOtBookingId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "OT booking not found"));
        MasSurgery surgery = request.getSurgeryId() == null ? null : surgeryRepository.findById(request.getSurgeryId())
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "Surgery not found"));

        String actor = currentActor();
        LocalDateTime now = LocalDateTime.now();
        Path directory = consentDirectory();
        String extension = extensionOf(originalName);
        String storedName = UUID.randomUUID() + extension;
        Path target = directory.resolve(storedName).normalize();
        if (!target.startsWith(directory)) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Invalid document filename");
        }
        try {
            Files.createDirectories(directory);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            PatientConsent consent = PatientConsent.builder()
                    .admissionId(request.getAdmissionId())
                    .otBooking(booking)
                    .surgery(surgery)
                    .consentType(consentType)
                    .remarks(request.getRemarks())
                    .documentName(originalName)
                    .documentPath(target.toString())
                    .uploadedBy(actor)
                    .uploadedDatetime(now)
                    .status("Y")
                    .lastChgBy(actor)
                    .lastChgDate(now)
                    .build();
            PatientConsent saved = consentRepository.saveAndFlush(consent);
            PatientConsentProjection projection = consentRepository.findConsentDetails(
                            request.getAdmissionId(), request.getOtBookingId()).stream()
                    .filter(row -> row.getPatientConsentId().equals(saved.getPatientConsentId()))
                    .findFirst()
                    .orElseThrow(() -> new SDDException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unable to load saved consent"));
            return ResponseUtils.createSuccessResponse(mapper.toResponse(projection), new TypeReference<>() {});
        } catch (IOException e) {
            deleteQuietly(target);
            throw new SDDException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Unable to store consent document");
        } catch (RuntimeException e) {
            deleteQuietly(target);
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<PatientConsentResponse>> getConsents(Long admissionId, Long otBookingId) {
        if (admissionId == null && otBookingId == null) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Provide admissionId, otBookingId, or both");
        }
        List<PatientConsentResponse> result = consentRepository.findConsentDetails(admissionId, otBookingId)
                .stream().map(mapper::toResponse).toList();
        return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});
    }

    @Override
    @Transactional(readOnly = true)
    public PatientConsentFile getDocument(Long patientConsentId) {
        PatientConsent consent = consentRepository.findById(patientConsentId)
                .filter(row -> "Y".equalsIgnoreCase(row.getStatus()))
                .orElseThrow(() -> new SDDException(HttpStatus.NOT_FOUND.value(), "Patient consent document not found"));
        Path directory = consentDirectory();
        Path document = Paths.get(consent.getDocumentPath()).toAbsolutePath().normalize();
        if (!document.startsWith(directory) || !Files.isRegularFile(document) || !Files.isReadable(document)) {
            throw new SDDException(HttpStatus.NOT_FOUND.value(), "Patient consent document file not found");
        }
        String contentType;
        try {
            contentType = Files.probeContentType(document);
        } catch (IOException e) {
            contentType = null;
        }
        if (contentType == null) contentType = "application/octet-stream";
        return new PatientConsentFile(new FileSystemResource(document), consent.getDocumentName(), contentType);
    }

    private void validateDocument(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Consent document is required");
        }
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!List.of("application/pdf", "image/jpeg", "image/png").contains(contentType)) {
            throw new SDDException(HttpStatus.BAD_REQUEST.value(), "Only PDF, JPEG and PNG consent documents are allowed");
        }
    }

    private Path consentDirectory() {
        return Paths.get(CONSENT_DIRECTORY).toAbsolutePath().normalize();
    }

    private String extensionOf(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return "";
        String extension = filename.substring(dot).toLowerCase(Locale.ROOT);
        return List.of(".pdf", ".jpg", ".jpeg", ".png").contains(extension) ? extension : "";
    }

    private String currentActor() {
        UserContext context = userContextService.getCurrentUserContext();
        if (context != null && context.getUserFullName() != null && !context.getUserFullName().isBlank()) {
            return context.getUserFullName();
        }
        String actor = userContextService.getCurrentUserFullNameFromToken();
        if (actor == null || actor.isBlank()) throw new SDDException(HttpStatus.UNAUTHORIZED.value(), "Current user context not found");
        return actor;
    }

    private void deleteQuietly(Path path) {
        try { Files.deleteIfExists(path); } catch (IOException ignored) { }
    }
}
