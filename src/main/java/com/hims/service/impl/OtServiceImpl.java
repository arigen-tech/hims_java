package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.projection.PendingForOtProjection;
import com.hims.projection.PendingForOtSurgeryProjection;
import com.hims.request.OtRequest;
import com.hims.response.PendingForOtResponse;
import com.hims.service.TransactionSequenceService;
import com.hims.utils.AuthUtil;
import com.hims.utils.HMISTransaction;
import org.springframework.beans.factory.annotation.Value;
import com.hims.exception.SDDException;
import com.hims.projection.ActiveAdmissionOtProjection;
import com.hims.projection.ActiveAdmissionProjectionResponse;
import com.hims.request.OtBookingRequestDtDto;
import com.hims.request.OtBookingRequestHdDto;
import com.hims.response.ActiveAdmissionOtResponse;
import com.hims.response.ActiveAdmissionResponse;
import com.hims.response.ApiResponse;
import com.hims.service.OtService;
import com.hims.utils.ResponseUtils;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtServiceImpl implements OtService {

    private final OtBookingRequestHdRepository hdRepository;
    private final OtBookingRequestDtRepository dtRepository;
    private final InpatientRepository inpatientRepository;
    private final TransactionSequenceService transactionSequenceService;
    private final AuthUtil authUtil;
    private final PatientRepository patientRepository;
    private final MasSurgeryTypeRepository masSurgeryTypeRepository;
    private final MasSurgeryRepository masSurgeryRepository;
    private final MasOtBookingStatusRepository masOtBookingStatusRepository;
    private final MasOperationTheatreRepository masOperationTheatreRepository;
    private final UserRepo userRepo;
    private  final MasDepartmentRepository masDepartmentRepository;
    private final OtBookingRepository otBookingRepository;
    private final OtBookingStatusLogRepository otBookingStatusLogRepository;
    @Value("${ipd.admission.status.admitted}")
    Long ipdAdmissionStatusAdmitted;

    @Value("${surgery.booking-status.requested}")
    private Long surgeryBookingStatusRequested;


    @Transactional
    @Override
    public ApiResponse<String> createOrUpdateOtBookingHeader(OtBookingRequestHdDto dto) {
        if (dto == null) {
            throw new SDDException(
                    "otBooking",
                    400,
                    "OT Booking request cannot be null"
            );
        }

        if (dto.getPatientId() == null) {
            throw new SDDException(
                    "patient",
                    400,
                    "Patient ID is required"
            );
        }

        if (dto.getVisitId() == null) {
            throw new SDDException(
                    "visit",
                    400,
                    "Visit ID is required"
            );
        }

        // ================= HEADER =================

        OtBookingRequestHd hd;

        if (dto.getOtBookingRequestId() != null) {
            // UPDATE existing booking
            hd = hdRepository.findById(dto.getOtBookingRequestId())
                    .orElseThrow(() ->
                            new SDDException(
                                    "otBooking",
                                    404,
                                    "OT Booking Request not found with ID: "
                                            + dto.getOtBookingRequestId()
                            )
                    );
        } else {
            Optional<OtBookingRequestHd> optionalHd = hdRepository.findByVisitId(dto.getVisitId());
            if (optionalHd.isPresent()) {
                hd = optionalHd.get();
            } else {
                hd = new OtBookingRequestHd();
                hd.setRequestNo(dto.getRequestNo());
            }
        }

        hd.setPatientId(patientRepository.findById(dto.getPatientId()).orElseThrow());
        hd.setVisitId(dto.getVisitId());
        hd.setDepartmentId(masDepartmentRepository.findById(dto.getDepartmentId()).orElseThrow());
        hd.setPrimarySurgeonId(userRepo.findById(dto.getPrimarySurgeonId()).orElseThrow());
        hd.setDiagnosis(dto.getDiagnosis());
        hd.setRequestSource(dto.getRequestSource());
        hd.setPreferredOtId(masOperationTheatreRepository.findById(dto.getPreferredOtId()).orElseThrow());
        hd.setPreferredDate(dto.getPreferredDate());
        hd.setPreferredStartTime(dto.getPreferredStartTime());
        hd.setPreferredEndTime(dto.getPreferredEndTime());
        hd.setStatus(dto.getStatus());
        hd.setBookingStatusId(masOtBookingStatusRepository.findById(dto.getBookingStatusId()).orElseThrow());
        hd.setPriority(dto.getPriority());
        hd.setRequestedBy(dto.getRequestedBy());
        hd.setRequestedDate(dto.getRequestedDate());
        hd.setLastChgBy(dto.getLastChgBy());
        hd.setLastChgDate(LocalDateTime.now());
        OtBookingRequestHd savedHd = hdRepository.save(hd);

        log.info("OT Booking Header saved successfully. ID: {}",savedHd.getOtBookingRequestId());
        // ================= DETAILS =================
        saveOrUpdateOtDetails(savedHd,dto.getSurgeryDetails());
        log.info("OT Booking Details saved successfully. Header ID: {}", savedHd.getOtBookingRequestId());

        // ================= RESPONSE =================
        return ResponseUtils.createSuccessResponse(
                "OT Booking saved successfully",
                new TypeReference<>() {
                }
        );
    }

    @Override
    public ApiResponse<Page<ActiveAdmissionOtResponse>> activeAdmissionList(int page, int size, String patientName, String mobileNo, String admissionNo, Long wardId) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "inpatientId"));
            Page<ActiveAdmissionOtProjection> projectionPage = inpatientRepository.activeAdmissionList(ipdAdmissionStatusAdmitted,

                    patientName,
                    mobileNo,
                    admissionNo,
                    wardId,
                    pageable
            );
            Page<ActiveAdmissionOtResponse> responsePage = projectionPage.map(this::mapActiveAdmissionOtResponse);

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error while fetching active admission list. ");
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    "Error while fetching active admission list: " + e.getMessage(), 500);
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> saveOtRequest(OtRequest request) {
        try {
            User user = authUtil.getCurrentUser();
            Inpatient inpatient = inpatientRepository.findById(request.getInpatientId())
                    .orElseThrow(() -> new SDDException(404,"Inpatient not found with ID: " + request.getInpatientId()));
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new SDDException(404,"Patient not found with ID: " + request.getPatientId()));
            User primarySurgeon = userRepo.findById(request.getPrimarySurgeonId())
                    .orElseThrow(() -> new SDDException(404,"Primary surgeon not found with ID: " + request.getPrimarySurgeonId()));
            MasOperationTheatre operationTheatre = masOperationTheatreRepository.findById(request.getPreferredOtId())
                            .orElseThrow(() -> new SDDException(404,"Operation theatre not found with ID: " + request.getPreferredOtId()));
            MasOtBookingStatus bookingStatus = masOtBookingStatusRepository.findById(surgeryBookingStatusRequested)
                            .orElseThrow(() -> new SDDException(404,"OT booking status not found with ID: " + surgeryBookingStatusRequested));

            OtBookingRequestHd header = new OtBookingRequestHd();
            header.setRequestNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.SURGERY_NO, request.getHospitalId()));
            header.setPatientId(patient);
            header.setVisitId(request.getVisitId());
            header.setRequestSource(AppConstants.SOURCE_TYPE_IPD);
            header.setPrimarySurgeonId(primarySurgeon);
            header.setDiagnosis(request.getDiagnosis());
            header.setPriority(request.getPriority().toUpperCase());
            header.setPreferredDate(request.getPreferredDate());
            header.setAdmissionId(inpatient);
            header.setPreferredOtId(operationTheatre);
            header.setBookingStatusId(bookingStatus);
            header.setRequestedBy(user.getFullName());
            header.setDepartmentId(masDepartmentRepository.findById(request.getDepartmentId()).orElseThrow());
            header.setPreferredStartTime(request.getPreferredStartTime());
            header.setPreferredEndTime(request.getPreferredEndTime());
            header.setRequestedDate(LocalDateTime.now());
            header.setSpecialInstruction(request.getSpecialInstructions());
            header.setStatus(AppConstants.STATUS_N);
            header.setLastChgBy(user.getFullName());
            header.setLastChgDate(LocalDateTime.now());

            OtBookingRequestHd savedHeader = hdRepository.save(header);

            log.info("OT booking header saved | requestId={}, requestNo={}, patientId={}, inpatientId={}",
                    savedHeader.getOtBookingRequestId(),
                    savedHeader.getRequestNo(),
                    request.getPatientId(),
                    request.getInpatientId()
            );

            MasSurgeryType surgeryType = masSurgeryTypeRepository.findById(request.getSurgeryTypeId()).orElseThrow(() ->
                                    new SDDException(404,"Surgery type not found with ID: " + request.getSurgeryTypeId()));
            MasSurgery surgery = masSurgeryRepository.findById(request.getSurgeryId())
                    .orElseThrow(() -> new SDDException(404,"Surgery not found with ID: " + request.getSurgeryId()));

            OtBookingRequestDt detail = new OtBookingRequestDt();
            detail.setOtBookingRequest(savedHeader);
            detail.setSurgeryTypeId(surgeryType);
            detail.setSurgeryId(surgery);
            detail.setSequenceNo(1L);
            detail.setExpectedDurationMin(request.getExpectedDuration());
            detail.setStatus(AppConstants.STATUS_N);
            detail.setLastChgBy(user.getFullName());
            detail.setLastChgDate(LocalDateTime.now());
            OtBookingRequestDt savedDetail = dtRepository.save(detail);
            log.info("OT booking request completed successfully | requestId={}, requestNo={}", savedHeader.getOtBookingRequestId(), savedHeader.getRequestNo());
            return ResponseUtils.createSuccessResponse("OT booking request saved successfully", new TypeReference<>() {});

       } catch (Exception e) {
            throw e;
        }
    }
    @Override
    @Transactional
    public ApiResponse<Page<PendingForOtResponse>> pendingForReviewOt(int page, int size, String patientName, String mobileNo, String patientType) {

        log.info("Fetching pending OT review list, page={}, size={}, patientName={}, mobileNo={}, patientType={}", page, size, patientName, mobileNo, patientType);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "otBookingRequestId"));

        Page<PendingForOtProjection> projectionPage = hdRepository.findPendingForOt(surgeryBookingStatusRequested,patientName, mobileNo, patientType, pageable);

        // Get IDs from current page
        List<Long> requestIds = projectionPage.getContent().stream().map(PendingForOtProjection::getOtBookingRequestId).distinct().toList();

        // Fetch surgeries for current page only
        List<PendingForOtSurgeryProjection> surgeryProjections = dtRepository.findSurgeriesByRequestIds(requestIds);

        // Group surgeries by OT booking request
        Map<Long, List<PendingForOtResponse.SurgeryResponse>> surgeryMap = surgeryProjections.stream()
                        .collect(Collectors.groupingBy(PendingForOtSurgeryProjection::getOtBookingRequestId,
                                Collectors.mapping(surgery -> {
                                    PendingForOtResponse.SurgeryResponse response = new PendingForOtResponse.SurgeryResponse();
                                    response.setOtBookingDtId(surgery.getOtBookingDtId());
                                    response.setSurgeryId(surgery.getSurgeryId());
                                    response.setSurgeryName(surgery.getSurgeryName());
                                    return response;
                                }, Collectors.toList())));

        Page<PendingForOtResponse> responsePage = projectionPage.map(projection -> {
                    PendingForOtResponse response = new PendingForOtResponse();
                    response.setOtBookingRequestId(projection.getOtBookingRequestId());
                    response.setInpatientId(projection.getInpatientId());
                    response.setVisitId(projection.getVisitId());
                    response.setPatientId(projection.getPatientId());
                    response.setPatientName(projection.getPatientName());
                    response.setUhid(projection.getUhid());
                    response.setAge(projection.getAge());
                    response.setGenderId(projection.getGenderId());
                    response.setGender(projection.getGender());
                    response.setMobileNo(projection.getMobileNo());
                    response.setAdmissionNo(projection.getAdmissionNo());
                    response.setSurgeonId(projection.getSurgeonId());
                    response.setSurgeonName(projection.getSurgeonName());
                    response.setPatientType(projection.getPatientType());
                    response.setOtId(projection.getOtId());
                    response.setOtName(projection.getOtName());
                    response.setRequestedDate(projection.getRequestedDate());
                    response.setRequestedTime(projection.getRequestedTime());
                    response.setRequestedBy(projection.getRequestedBy());
                    response.setPriority(projection.getPriority());
                    response.setRequestedNo(projection.getRequestedNo());
                    response.setSurgeryResponses(surgeryMap.getOrDefault(projection.getOtBookingRequestId(), new ArrayList<>()));
                    return response;});

        log.info("Pending OT review list fetched successfully, totalElements={}", responsePage.getTotalElements());
        return ResponseUtils.createSuccessResponse( responsePage,new TypeReference<>(){});
    }
    @Override
    @Transactional
    public ApiResponse<String> saveAcceptAndReject(Long otBookingRequestId, String flag) {
        log.info("Processing OT request, otBookingRequestId={}, flag={}", otBookingRequestId, flag);
        try {
        User user = authUtil.getCurrentUser();

        if (!flag.equalsIgnoreCase("A") && !flag.equalsIgnoreCase("R")) {
            return ResponseUtils.createNotFoundResponse("Invalid flag. Use A for Accept or R for Reject", 400);
        }
        OtBookingRequestHd request = hdRepository.findById(otBookingRequestId)
                        .orElseThrow(() -> new RuntimeException("OT booking request not found: " + otBookingRequestId));

        MasOtBookingStatus fromStatus = request.getBookingStatusId();

        Long statusId;
        if (flag.equalsIgnoreCase("A")) {
            statusId = 3L;
        } else {
            statusId = 2L;
        }
        MasOtBookingStatus toStatus = masOtBookingStatusRepository.findById(statusId)
                        .orElseThrow(() -> new RuntimeException("OT booking status not found: " + statusId));

        request.setBookingStatusId(toStatus);
        LocalDateTime now = LocalDateTime.now();
        String currentUser = user.getFullName();

        OtBooking booking = OtBooking.builder()
                .bookingNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.OT_BOOKING_NO, user.getHospital().getId()))
                .otBookingRequest(request)
                .operationTheatre(request.getPreferredOtId())
                .scheduledDate(request.getPreferredDate())
                .scheduledStartTime(request.getPreferredStartTime())
                .scheduledEndTime(request.getPreferredEndTime())
                .bookingStatus(toStatus)
                .bookedBy(currentUser)
                .bookedDate(now)
                .status("Y")
                .lastChgBy(currentUser)
                .lastChgDate(now)
                .isPacRequired("N")
                .isPacDone("N")
                .build();

        booking = otBookingRepository.save(booking);

        OtBookingStatusLog statusLog = OtBookingStatusLog.builder()
                .otBooking(booking)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .changedBy(currentUser)
                .changedDate(now)
                .status("Y")
                .lastChgBy(currentUser)
                .lastChgDate(now)
                .build();
        otBookingStatusLogRepository.save(statusLog);
        hdRepository.save(request);
        String message = flag.equalsIgnoreCase("A") ? "OT booking request accepted successfully" : "OT booking request rejected successfully";
        return ResponseUtils.createSuccessResponse(message, new TypeReference<String>() {});
        } catch (Exception e) {

            throw new RuntimeException("Failed to process OT booking request: " + otBookingRequestId, e);
        }
    }

    private void saveOrUpdateOtDetails(OtBookingRequestHd hd, List<OtBookingRequestDtDto> requestDetails) {

        log.info("Saving OT details for Header ID: {}",hd.getOtBookingRequestId());

        if (requestDetails == null) {
            requestDetails = Collections.emptyList();
        }

        // Existing DB details
        List<OtBookingRequestDt> existingDetails = dtRepository.findByOtBookingRequest(hd);

        Map<Long, OtBookingRequestDt> existingMap =
                existingDetails.stream()
                        .filter(Objects::nonNull)
                        .filter(dt -> dt.getOtBookingRequestDtId() != null)
                        .collect(Collectors.toMap(
                                OtBookingRequestDt::getOtBookingRequestDtId,
                                Function.identity()
                        ));

        Set<Long> requestDetailIds = new HashSet<>();
        List<OtBookingRequestDt> detailsToSave = new ArrayList<>();
        long sequence = 1;
        for (OtBookingRequestDtDto dto : requestDetails) {
            if (dto == null) {
                continue;
            }
            OtBookingRequestDt dt;
            // ================= UPDATE =================
            if (dto.getOtBookingRequestDtId() != null) {
                dt = existingMap.get(dto.getOtBookingRequestDtId());
                if (dt == null) {
                    throw new SDDException(
                            "otBookingDetail",
                            404,
                            "OT Booking Detail not found with ID: "
                                    + dto.getOtBookingRequestDtId()
                    );
                }

                requestDetailIds.add(dto.getOtBookingRequestDtId());
            }
            // ================= CREATE =================
            else {
                dt = new OtBookingRequestDt();
                dt.setOtBookingRequest(hd);
            }
            // ================= MAP DATA =================
            dt.setOtBookingRequest(hd);
            dt.setSurgeryId(masSurgeryRepository.findById(dto.getSurgeryId()).orElseThrow());
            dt.setSurgeryTypeId(masSurgeryTypeRepository.findById(dto.getSurgeryTypeId()).orElseThrow());
            dt.setSequenceNo(sequence++);
            dt.setExpectedDurationMin(dto.getExpectedDurationMin());
            dt.setStatus(AppConstants.STATUS_N);
            dt.setLastChgBy(dto.getLastChgBy());
            dt.setLastChgDate(LocalDateTime.now());
            detailsToSave.add(dt);
        }
        // ================= SAVE =================
        if (!detailsToSave.isEmpty()) {
            dtRepository.saveAll(detailsToSave);
            log.info(
                    "Saved {} OT booking details for Header ID: {}",
                    detailsToSave.size(),
                    hd.getOtBookingRequestId()
            );
        }

        // ================= DELETE REMOVED DETAILS =================

        List<OtBookingRequestDt> detailsToDelete =
                existingDetails.stream()
                        .filter(existing ->
                                existing.getOtBookingRequestDtId() != null
                                        && !requestDetailIds.contains(
                                        existing.getOtBookingRequestDtId()
                                )
                        )
                        .toList();

        if (!detailsToDelete.isEmpty()) {
            dtRepository.deleteAll(detailsToDelete);
            log.info(
                    "Deleted {} removed OT booking details for Header ID: {}",
                    detailsToDelete.size(),
                    hd.getOtBookingRequestId()
            );
        }
    }
    private ActiveAdmissionOtResponse mapActiveAdmissionOtResponse(
            ActiveAdmissionOtProjection projection) {

        ActiveAdmissionOtResponse response = new ActiveAdmissionOtResponse();

        response.setInpatientId(projection.getInpatientId());
        response.setVisitId(projection.getVisitId());
        response.setPatientId(projection.getPatientId());
        response.setPatientName(projection.getPatientName());
        response.setUhid(projection.getUhid());
        response.setAge(projection.getAge());
        response.setGenderId(projection.getGenderId());
        response.setGender(projection.getGender());
        response.setMobileNo(projection.getMobileNo());
        response.setAdmissionNo(projection.getAdmissionNo());
        response.setWardId(projection.getWardId());
        response.setWard(projection.getWard());
        response.setRooId(projection.getRooId());
        response.setRoom(projection.getRoom());
        response.setBedId(projection.getBedId());
        response.setBed(projection.getBed());
        response.setDiagnosis(projection.getDiagnosis());
        response.setAdmissionDateTime(projection.getAdmissionDateTime());
        response.setDoctorName(projection.getDoctorName());

        return response;
    }
}