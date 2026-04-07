package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.LabService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Service
@RequiredArgsConstructor
@Slf4j
public class LabServiceImpl implements LabService {

    private final LabHdRepository labOrderHdRepository;

    private final LabDtRepository labOrderDtRepository;

    private final VisitRepository visitRepository;

    private final DgSampleCollectionHeaderRepository dgSampleCollectionHeaderRepository;

    private  final DgSampleCollectionDetailsRepository dgSampleCollectionDetailsRepository;

    private final MasDepartmentRepository masDepartmentRepository;

    private final MasSubChargeCodeRepository masSubChargeCodeRepository;

    private final DgMasCollectionRepository dgMasCollectionRepository;

    private final DgMasInvestigationRepository dgMasInvestigationRepository;

    private final DgMasSampleRepository dgMasSampleRepository;

    private final LabTurnAroundTimeRepository labTurnAroundTimeRepository;

    private final LabOrderTrackingStatusRepository labOrderTrackingStatusRepository;

    private final DgSubMasInvestigationRepository dgSubMasInvestigationRepository;

    private final DgFixedValueRepository dgFixedValueRepository;

    private final DgResultEntryHeaderRepository dgResultEntryHeaderRepository;

    private final DgResultEntryDetailRepository dgResultEntryDetailRepository;

    private final MasLabResultAmendmentTypeRepository masLabResultAmendmentTypeRepository;

    private final LabResultAmendAuditRepository labResultAmendAuditRepository;

    private  final AuthUtil authUtil;

    @Value("${sample.collection.display.days}")
    private int pendingDaysForCollectionDisplay;


    @Value("${lab.track-order-status-result.validate}")
    private Long resultValidatedStatusId;


    @Value("${lab.track-order-status-sample.collect}")
    private Long sampleCollectStatusId;

    @Value("${lab.track-order-status-sample.validate}")
    private Long sampleValidateStatusId;

    @Value("${lab.track-order-status-sample.reject}")
    private Long sampleRejectStatusId;

    @Value("${lab.track-order-status-result.entry}")
    private Long resultEnteredStatusId;



    @Override
    public ApiResponse<Page<PendingSampleHeaderResponse>> getPendingSampleHeadersForCollection(Long hospitalId, String patientName, String patientMobileNumber, int page, int size) {
        try {
            log.info("getPendingSamples method started with hospitalId-{} , patientName-{}, patientMobileNumber-{}", hospitalId, patientName, patientMobileNumber);
            if (patientName != null && !patientName.trim().isEmpty()) {
                patientName = "%" + patientName.trim().toLowerCase() + "%";
            } else {
                patientName = null;
            }
            LocalDate startDate = LocalDate.now().minusDays(pendingDaysForCollectionDisplay); // example
            LocalDate endDate = LocalDate.now();

            List<String> paymentStatuses = Stream.of(AppConstants.STATUS_P, AppConstants.STATUS_Y).map(String::toLowerCase).toList();
            List<String> orderStatuses = Stream.of(AppConstants.STATUS_P, AppConstants.STATUS_N).map(String::toLowerCase).toList();

            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderTime"));
            Page<PendingSampleHeaderResponse> result = labOrderHdRepository.findPendingSamples(
                    hospitalId,
                    patientName,
                    patientMobileNumber,
                    startDate,
                    endDate,
                    paymentStatuses,
                    orderStatuses,
                    pageable
            );
            log.info("getPendingSamples method started with hospitalId-{} , patientName-{}, patientMobileNumber-{}", hospitalId, patientName, patientMobileNumber);

            return  ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});
        }catch (Exception e){
            log.error("getPendingSamples method error :: ", e);
            return ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<List<PendingSampleDetailResponse>> getPendingSampleDetailsForCollection(Long orderHdId) {
       try {
           log.info("getPendingSampleDetails method started with orderHdId-{}", orderHdId);
           List<PendingSampleDetailResponse> details = labOrderDtRepository.findPendingDetailsForCollectionByOrderHdId(orderHdId,AppConstants.STATUS_Y.toLowerCase(),AppConstants.STATUS_N.toLowerCase());
           log.info("getPendingSampleDetails method ended for orderHdId-{}", orderHdId);
           return ResponseUtils.createSuccessResponse(details, new TypeReference<>() {});
       } catch (Exception e) {
           log.error("getPendingSampleDetailsForCollection method error :: ", e);
           return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},AppConstants.INTERNAL_SERVER_ERR_MSG,HttpStatus.INTERNAL_SERVER_ERROR.value());
       }
    }

    @Override
    @Transactional
    public ApiResponse<AppsetupResponse> savePendingSamplesForCollection(Long departmentId,SampleCollectionRequest sampleReq) {

        AppsetupResponse res = new AppsetupResponse();
        log.info("Sample collection process started");

        try {

            User currentUser = authUtil.getCurrentUser();

            Optional<Visit> visitOpt = visitRepository.findById((long) sampleReq.getVisitId());
            if (visitOpt.isEmpty()) {
                log.warn("Visit not found, visitId={}", sampleReq.getVisitId());
                return ResponseUtils.createNotFoundResponse(AppConstants.VISIT_NOT_FOUND_ERR_MSG, HttpStatus.NOT_FOUND.value());
            }
            Visit visit = visitOpt.get();

            LocalDateTime now = LocalDateTime.now();

            String fullName = Stream.of(
                            currentUser.getFirstName(),
                            currentUser.getMiddleName(),
                            currentUser.getLastName()
                    ).filter(Objects::nonNull)
                    .collect(Collectors.joining(" "));

            //GROUP BY MODALITY (SubChargeCodeId)
            Map<Integer, List<SampleCollectionInvestigationReq>> groupedData =
                    sampleReq.getSampleCollectionReq()
                            .stream()
                            .collect(Collectors.groupingBy(
                                    SampleCollectionInvestigationReq::getSubChargeCodeId
                            ));
            log.info("Total modalities found={}", groupedData.size());

            for (Map.Entry<Integer, List<SampleCollectionInvestigationReq>> entry : groupedData.entrySet()) {

                Integer subChargeCodeId = entry.getKey();
                List<SampleCollectionInvestigationReq> detailsList = entry.getValue();
                log.debug("Processing SubChargeCodeId={}, itemCount={}",
                        subChargeCodeId, detailsList.size());

                //FIND OR CREATE HEADER
                Optional<DgSampleCollectionHeader> existingHeaderOpt =
                        dgSampleCollectionHeaderRepository
                                .findByVisitIdAndSubChargeCodeAndValidateStatusN(
                                        visit.getId(),
                                        subChargeCodeId.longValue()
                                );

                DgSampleCollectionHeader header;

                if (existingHeaderOpt.isPresent()) {

                    header = existingHeaderOpt.get();
                    log.debug("Existing header found, headerId={}",
                            header.getSampleCollectionHeaderId());
                } else {
                    header = new DgSampleCollectionHeader();

                    header.setVisitId(visit);
                    header.setPatientId(visit.getPatient());
                    header.setHospitalId(currentUser.getHospital());

                    MasDepartment department =
                            masDepartmentRepository.findById(departmentId).orElseThrow();
                    header.setDepartmentId(department);

                    MasSubChargeCode subChargeCode =
                            masSubChargeCodeRepository.findById(subChargeCodeId.longValue())
                                    .orElseThrow();

                    header.setSubChargeCode(subChargeCode);

                    header.setCollection_by(fullName);
                    header.setCollection_time(now);

                    header.setLastChgBy(fullName);
                    header.setLastChgDate(now);
                    header.setLastChgTime(now);

                    header.setValidated("n");
                    header.setPriority("r");
                    header.setSampleOrderStatus("n");
                    header.setResult_entry_status("n");

                    dgSampleCollectionHeaderRepository.save(header);
                    log.info("New sample collection header created, headerId={}",
                            header.getSampleCollectionHeaderId());
                }

                // GROUP DETAILS BY PHYSICAL CONTAINER (sampleId)
                Map<Integer, List<SampleCollectionInvestigationReq>>

                        containerWiseMap =
                        detailsList.stream()
                                .collect(Collectors.groupingBy(
                                        SampleCollectionInvestigationReq::getSampleId
                                ));

                int dailySequence = dgSampleCollectionDetailsRepository.getNextSequenceValue().intValue();


                //GENERATE ONE SAMPLE ID PER CONTAINER
                for (Map.Entry<Integer, List<SampleCollectionInvestigationReq>> containerEntry
                        : containerWiseMap.entrySet()) {

                    Integer key = containerEntry.getKey();

                    DgMasCollection container = dgMasCollectionRepository.findById(key.longValue()).orElseThrow(() -> new RuntimeException("Invalid container id"));
                    String containerSuffix=container.getCollectionCode();
                    String sampleId =
                            generateSampleId(
                                    header.getSubChargeCode().getSubCode(), // modality
                                    dailySequence++,
                                    containerSuffix
                            );
                    log.info("Generated sampleId={} for containerId={}", sampleId, key);

                    for (SampleCollectionInvestigationReq detailReq : containerEntry.getValue()) {

                        DgSampleCollectionDetails detail =
                                new DgSampleCollectionDetails();

                        detail.setSampleCollectionHeader(header);
                        detail.setRemarks(detailReq.getRemarks());
                        detail.setSampleGeneratedId(sampleId);

                        DgMasInvestigation investigation =
                                dgMasInvestigationRepository
                                        .findById((long) detailReq.getInvestigationId())
                                        .orElseThrow();

                        detail.setInvestigationId(investigation);
                        detail.setEmpanelledStatus(detailReq.getEmpanelledStatus());

                        DgMasSample masSample =
                                dgMasSampleRepository
                                        .findById((long) detailReq.getSampleId())
                                        .orElseThrow();

                        detail.setSampleId(masSample);

                        detail.setOrderStatus("n");
                        detail.setValidated("n");
                        detail.setResult_status("n");
                        detail.setSampleCollDatetime(now);

                        dgSampleCollectionDetailsRepository.save(detail);

                        //TURN AROUND TIME
                        LabTurnAroundTime tat = new LabTurnAroundTime();
                        tat.setInvestigation(investigation);
                        tat.setOrderHd(
                                labOrderHdRepository
                                        .findById(sampleReq.getOrderHdId())
                                        .orElseThrow()
                        );
                        tat.setPatient(visit.getPatient());
                        tat.setSampleCollectionDateTime(now);
                        tat.setSampleCollectedBy(fullName);
                        tat.setGeneratedSampleId(sampleId);

                        labTurnAroundTimeRepository.save(tat);
                        log.debug("Saved investigationId={} with sampleId={}",
                                investigation.getInvestigationId(), sampleId);
                    }
                }
            }
            log.info("Updating order status for orderHdId={}", sampleReq.getOrderHdId());

            // UPDATE ORDER DETAILS STATUS
            List<DgOrderDt> orderDetails =
                    labOrderDtRepository.findByOrderhdIdId(sampleReq.getOrderHdId());

            Set<Long> requestedInvestigationIds =
                    sampleReq.getSampleCollectionReq()
                            .stream()
                            .map(req -> (long) req.getInvestigationId())
                            .collect(Collectors.toSet());

            for (DgOrderDt orderDetail : orderDetails) {
                if (orderDetail.getInvestigationId() != null &&
                        requestedInvestigationIds.contains(
                                orderDetail.getInvestigationId().getInvestigationId()) &&
                       AppConstants.STATUS_Y.equalsIgnoreCase(orderDetail.getBillingStatus())) {

                    orderDetail.setOrderStatus(AppConstants.STATUS_Y.toLowerCase());
                    orderDetail.setOrderTrackingStatus(labOrderTrackingStatusRepository.findById(sampleCollectStatusId).orElseThrow());
                    labOrderDtRepository.save(orderDetail);
                }
            }

            // UPDATE ORDER HEADER STATUS (UNCHANGED)
            boolean allCompleted = true;
            boolean anyCompleted = false;

            for (DgOrderDt orderDetail : orderDetails) {
                if (AppConstants.STATUS_Y.equalsIgnoreCase(orderDetail.getOrderStatus())) {
                    anyCompleted = true;
                } else {
                    allCompleted = false;
                }
            }

            boolean finalAllCompleted = allCompleted;
            boolean finalAnyCompleted = anyCompleted;
            labOrderHdRepository.findById(sampleReq.getOrderHdId())
                    .ifPresent(hd -> {
                        if (finalAllCompleted) {
                            hd.setOrderStatus(AppConstants.STATUS_Y.toLowerCase());
                        } else if (finalAnyCompleted) {
                            hd.setOrderStatus(AppConstants.STATUS_P.toLowerCase());
                        } else {
                            hd.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
                        }
                        labOrderHdRepository.save(hd);
                    });

            res.setMsg(AppConstants.SUCCESS_MSG);
            log.info("Sample collection completed successfully");
            return ResponseUtils.createSuccessResponse(res, new TypeReference<AppsetupResponse>() {});

        } catch (Exception e) {
            log.error("Error during sample collection process", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ApiResponse<Page<SampleHeaderForValidationResponse>> getSampleHeaderForValidation(Long hospitalId, String patientName, String patientMobileNumber, int page, int size) {
        try {
            log.info("getSampleHeaderForValidation method started for hospitalId={}", hospitalId);
            if (patientName != null && !patientName.trim().isEmpty()) {
                patientName = "%" + patientName.trim().toLowerCase() + "%";
            } else {
                patientName = null;
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "collection_time"));
            Page<SampleHeaderForValidationResponse> sampleHeadersForValidation = dgSampleCollectionHeaderRepository
                    .findSampleHeadersForValidation(hospitalId, AppConstants.STATUS_N.toLowerCase(), patientName, patientMobileNumber, pageable);

            log.info("getSampleHeaderForValidation method ended for hospitalId={}", hospitalId);
            return ResponseUtils.createSuccessResponse(sampleHeadersForValidation, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getSampleHeaderForValidation method error :: ", e);
            return  ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<SampleDetailsForValidationResponse>> getSampleDetailsForValidationWrtHeader(Long sampleCollectionHeaderId) {
        try {
            log.info("getSampleDetailsForValidation method started for sampleCollectionHeaderId={}", sampleCollectionHeaderId);

            List<SampleDetailsForValidationResponse> detailsByHeaderId = dgSampleCollectionDetailsRepository
                    .findDetailsByHeaderId(sampleCollectionHeaderId,Stream.of(AppConstants.STATUS_P, AppConstants.STATUS_N).map(String::toLowerCase).toList());

            log.info("getSampleDetailsForValidation method ended for sampleCollectionHeaderId={}", sampleCollectionHeaderId);
            return ResponseUtils.createSuccessResponse(detailsByHeaderId, new TypeReference<>() {});
        }catch (Exception e) {
            log.error("getSampleDetailsForValidationWrtHeader method error for sampleCollectionHeaderId-{}:: ",sampleCollectionHeaderId, e);
            return  ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> validateInvestigations(List<InvestigationValidationRequest> requests) {

        try {
            log.info("Investigation validation process started...");
            log.debug("Total investigations received for validation={}",
                    requests != null ? requests.size() : 0);

            // =====================  CURRENT USER =====================
            User currentUser = authUtil.getCurrentUser();


            String validatedBy =
                    currentUser.getFirstName() + " " +
                            currentUser.getMiddleName() + " " +
                            currentUser.getLastName();
            log.debug("Validation performed by={}", validatedBy);


            Long headerId = requests.get(0).getSampleHeaderId();
            log.info("SampleCollectionHeaderId={}", headerId);

            // ===================== 1. FETCH HEADER ONCE =====================
            DgSampleCollectionHeader header =
                    dgSampleCollectionHeaderRepository.findById(headerId)
                            .orElseThrow(() -> new RuntimeException(AppConstants.SAMPLE_COLLECTION_HEADER_NOT_FOUND_ERR_MSG));
            log.debug("Header fetched successfully");

            // ===================== 2. FETCH ORDER HD ONCE =====================
            DgOrderHd orderHd =
                    labOrderHdRepository.findByPatientId_IdAndVisitId_Id(
                            header.getPatientId().getId(),
                            header.getVisitId().getId()
                    );

            // ===================== 3. CACHE TRACKING STATUS =====================
            LabOrderTrackingStatus validatedStatus =
                    labOrderTrackingStatusRepository
                            .findById(sampleValidateStatusId).orElseThrow();

            LabOrderTrackingStatus rejectedStatus =
                    labOrderTrackingStatusRepository
                            .findById(sampleRejectStatusId).orElseThrow();

            // ===================== 4. LOOP ALL REQUESTS =====================
            for (InvestigationValidationRequest req : requests) {

                DgSampleCollectionDetails details =
                        dgSampleCollectionDetailsRepository.findById(req.getDetailId())
                                .orElseThrow(() ->
                                        new RuntimeException(AppConstants.SAMPLE_COLLECTION_DETAIL_NOT_FOUND_ERR_MSG));

                boolean accepted = Boolean.TRUE.equals(req.getAccepted());
                String detailStatus = accepted ? AppConstants.STATUS_Y.toLowerCase() : AppConstants.STATUS_R.toLowerCase();

                Long investigationId =
                        details.getInvestigationId().getInvestigationId();

                int orderHdId =
                        header.getVisitId()
                                .getBillingHd()
                                .getHdorder()
                                .getId();

                // ===================== 5. LAB TURN AROUND TIME =====================
                LabTurnAroundTime tat =
                        labTurnAroundTimeRepository
                                .findByOrderHd_IdAndInvestigation_InvestigationIdAndPatient_IdAndGeneratedSampleId(
                                        orderHdId,
                                        investigationId,
                                        header.getPatientId().getId(),
                                        details.getSampleGeneratedId()
                                );

                tat.setIsReject(!accepted);
                tat.setSampleValidatedBy(validatedBy);
                tat.setSampleValidatedDateTime(LocalDateTime.now());
                labTurnAroundTimeRepository.save(tat);
                log.debug("TAT updated for investigationId={}", investigationId);

                // ===================== 6. UPDATE SAMPLE DETAILS =====================
                dgSampleCollectionDetailsRepository.updateValidation(
                        details.getSampleCollectionDetailsId(),
                        detailStatus
                );

                details.setValidated(detailStatus);

                if (!accepted) {
                    details.setRejected_reason(req.getReason());
                    details.setOldSampleCollectionHdIdForReject(headerId);
                } else {
                    details.setRejected_reason(null);
                    details.setOldSampleCollectionHdIdForReject(null);
                }

                dgSampleCollectionDetailsRepository.save(details);

                // ===================== 7. UPDATE ORDER DT =====================
                if (orderHd != null) {

                    DgOrderDt orderDt =
                            labOrderDtRepository
                                    .findByOrderhdId_IdAndInvestigationId_InvestigationId(
                                            orderHd.getId(),
                                            investigationId
                                    );

                    if (orderDt != null) {

                        String orderDtStatus = accepted ? AppConstants.STATUS_Y.toLowerCase() : AppConstants.STATUS_N.toLowerCase();

                        labOrderDtRepository.updateOrderStatus(
                                (long) orderDt.getId(),
                                orderDtStatus
                        );

                        orderDt.setOrderStatus(orderDtStatus);
                        orderDt.setOrderTrackingStatus(
                                accepted ? validatedStatus : rejectedStatus
                        );

                        labOrderDtRepository.save(orderDt);
                    }
                }
            }

            // ===================== 8. UPDATE HEADER STATUS =====================
            List<String> headerStatuses =
                    dgSampleCollectionDetailsRepository.getValidationStatusOfHeader(headerId);

            boolean allAccepted =
                    headerStatuses.stream().allMatch(AppConstants.STATUS_Y.toLowerCase()::equals);

            boolean allRejected =
                    headerStatuses.stream().allMatch(AppConstants.STATUS_R.toLowerCase()::equals);

            String finalHeaderStatus =
                    allAccepted ? AppConstants.STATUS_Y.toLowerCase() :
                            allRejected ? AppConstants.STATUS_R.toLowerCase() : AppConstants.STATUS_Y.toLowerCase(); // partial = y

            header.setValidated(finalHeaderStatus);
            header.setValidation_date(LocalDate.now());
            header.setValidationTime(Instant.now());
            header.setValidatedBy(validatedBy);
            dgSampleCollectionHeaderRepository.save(header);

            // ===================== 9. UPDATE ORDER HD =====================
            if (orderHd != null) {

                List<String> orderDtStatuses =
                        labOrderDtRepository.getOrderStatusesOfOrderHd(
                                (long) orderHd.getId()
                        );

                boolean allOrderRejected =
                        orderDtStatuses.stream().allMatch(AppConstants.STATUS_N.toLowerCase()::equals);

                boolean allOrderAccepted =
                        orderDtStatuses.stream().allMatch(AppConstants.STATUS_Y.toLowerCase()::equals);

                String finalOrderStatus =
                        allOrderRejected ? AppConstants.STATUS_N.toLowerCase():
                                allOrderAccepted ? AppConstants.STATUS_Y.toLowerCase() : AppConstants.STATUS_P.toLowerCase();

                labOrderHdRepository.updateOrderStatus(
                        (long) orderHd.getId(),
                        finalOrderStatus
                );

                orderHd.setOrderStatus(finalOrderStatus);
                labOrderHdRepository.save(orderHd);
                log.info("OrderHd status updated, orderHdId={}, status={}",
                        orderHd.getId(), finalOrderStatus);
            }
            log.info("Investigation validation completed successfully");
            // ===================== 10. SUCCESS =====================
            return ResponseUtils.createSuccessResponse(
                    AppConstants.INVESTIGATION_VALIDATION_SUCCESS_MSG,
                    new TypeReference<String>() {}
            );

        } catch (Exception e) {

            log.error("Sample Validate Error :: ", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public ApiResponse<Page<SampleHeaderForResultEntryResponse>> getSampleHeaderForResultEntry(Long hospitalId, String patientName, String patientMobileNumber, int page, int size) {
        try {
            log.info("getSampleHeaderForResultEntry method started for hospitalId={}", hospitalId);
            if (patientName != null && !patientName.trim().isEmpty()) {
                patientName = "%" + patientName.trim().toLowerCase() + "%";
            } else {
                patientName = null;
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "collection_time"));
            Page<SampleHeaderForResultEntryResponse> headersForResultEntry = dgSampleCollectionHeaderRepository.findHeadersForResultEntry(hospitalId,
                    AppConstants.STATUS_N.toLowerCase(),
                    AppConstants.STATUS_Y.toLowerCase(),
                    patientName,
                    patientMobileNumber,
                    pageable);
            log.info("getSampleHeaderForResultEntry method ended for hospitalId={}", hospitalId);
            return  ResponseUtils.createSuccessResponse(headersForResultEntry,new  TypeReference<>() {});
        }catch (Exception e) {
            log.error("getSampleHeaderForResultEntry method Error :: ", e);
            return ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<InvestigationResultResponse>> getInvestigationsForResultEntry(Long sampleCollectionHeaderId) {

        try {
            log.info("getInvestigationsForResultEntry method started for sampleCollectionHeaderId={}", sampleCollectionHeaderId);
            List<InvestigationResultResponse> investigationsForResultEntry = dgSampleCollectionDetailsRepository.getInvestigationsForResultEntry(sampleCollectionHeaderId);
            log.info("getInvestigationsForResultEntry method ended for sampleCollectionHeaderId={}", sampleCollectionHeaderId);
            return ResponseUtils.createSuccessResponse(investigationsForResultEntry,new  TypeReference<>() {});
        } catch (Exception e) {
            log.error("Sample Validate Error for sampleCollectionHeaderId - {}:: ",sampleCollectionHeaderId, e);
            return ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<SubInvestigationResultResponse>> getSubInvestigationsForResultEntry(Long investigationId,String genderCode,String age) {
        try {
            log.info("getSubInvestigationsForResultEntry method started for investigationId -{}, genderCode- {}, age -{}",investigationId,genderCode,age);
            Long extractedAgeFromString = extractAgeFromString(age);
            List<SubInvestigationResultResponse> subInvestigationResultResponses = dgSubMasInvestigationRepository.findSubInvestigationsWrtInvAndGenderAndAge(investigationId, genderCode, extractedAgeFromString);
            log.info("getSubInvestigationsForResultEntry method ended for investigationId -{}, genderCode- {}, age -{}",investigationId,genderCode,age);
            return  ResponseUtils.createSuccessResponse(subInvestigationResultResponses,new  TypeReference<>() {});
        }catch (Exception e) {
            log.error("getSubInvestigationsForResultEntry Error for investigationId -{}, genderCode- {}, age -{}",investigationId,genderCode,age,e);
            return  ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<FixedValueResultResponse>> getFixedValuesResultDropdown(Long subInvestigationId) {

        try {
            log.info("getFixedValuesResultDropdown method started for subInvestigationId={}", subInvestigationId);

            List<FixedValueResultResponse> fixedValues =
                    dgFixedValueRepository.findFixedValuesBySubInvestigationId(subInvestigationId);

            log.info("Total fixed values fetched={}", fixedValues.size());
            log.info("getFixedValuesResultDropdown method started for subInvestigationId={}", subInvestigationId);
            return ResponseUtils.createSuccessResponse(
                    fixedValues,
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error fetching fixed values", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Page<SampleHeaderForResultValidationResponse>> getSampleHeaderForResultValidation(Long hospitalId, String patientName, String patientMobileNumber, int page, int size) {
        try {
            log.info("getSampleHeaderForResultValidation method started for hospitalId={}", hospitalId);
            if (patientName != null && !patientName.trim().isEmpty()) {
                patientName = "%" + patientName.trim().toLowerCase() + "%";
            } else {
                patientName = null;
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "resultDate", "resultTime"));
            Page<SampleHeaderForResultValidationResponse> response =
                    dgResultEntryHeaderRepository.getSampleHeaderForResultValidation(hospitalId, patientName, patientMobileNumber, pageable);
            log.info("getSampleHeaderForResultValidation method ended for hospitalId={}", hospitalId);
            return ResponseUtils.createSuccessResponse(
                    response,
                    new TypeReference<>() {}
            );
        } catch (Exception e) {
            log.error("getSampleHeaderForResultValidation method error for hospitalId={}:: ",hospitalId,e);
            return  ResponseUtils.createFailureResponse(null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }
    @Override
    public ApiResponse<List<InvestigationsForResultValidation>> getInvestigationsForResultValidation(Long resultEntryHeaderId) {
        try {
            log.info("getInvestigationsForResultValidation method started for resultEntryHeaderId={}", resultEntryHeaderId);
            List<InvestigationsForResultValidation> list =
                    dgResultEntryDetailRepository.getInvestigationsForResultValidationWrtHeader(resultEntryHeaderId,AppConstants.STATUS_N.toLowerCase());

            log.info("getInvestigationsForResultValidation method ended for resultEntryHeaderId={}", resultEntryHeaderId);

            return ResponseUtils.createSuccessResponse(list, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getInvestigationsForResultValidation method error for resultEntryHeaderId={} :: ", resultEntryHeaderId,e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }
    @Override
    public ApiResponse<List<SubInvestigationsForResultValidationResponse>> getSubInvestigationsForResultValidation(Long resultEntryDetailId,Long investigationId) {

        try {
            log.info("getSubInvestigationsForResultValidation method started for resultEntryDetailId={}", resultEntryDetailId);

            List<SubInvestigationsForResultValidationResponse> list =
                    dgResultEntryDetailRepository.findSubInvestigationsByDetailId(resultEntryDetailId,investigationId,AppConstants.STATUS_N.toLowerCase());
            log.info("getSubInvestigationsForResultValidation method started for resultEntryDetailId={}", resultEntryDetailId);

            return ResponseUtils.createSuccessResponse(list, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getSubInvestigationsForResultValidation method error for resultEntryDetailId={}", resultEntryDetailId,e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    @Transactional
    public ApiResponse<String> updateAndValidateResult( ResultValidationUpdateRequest request) {
        log.info("Starting result validation. HeaderId={}",
                request.getResultEntryHeaderId());

        try {
            User currentUser = authUtil.getCurrentUser();


            //For Date Time Formating
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(AppConstants.TIME_FORMAT);


            // Fetch header
            Optional<DgResultEntryHeader> optionalHeader = dgResultEntryHeaderRepository.findById(request.getResultEntryHeaderId());
            if (optionalHeader.isEmpty()) {
                log.warn("Result entry header not found. HeaderId={}",
                        request.getResultEntryHeaderId());

                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        AppConstants.RESULT_ENTRY_HEADER_NOT_FOUND_ERR_MSG, HttpStatus.NOT_FOUND.value());
            }

            DgResultEntryHeader header = optionalHeader.get();
            log.info("Header fetched successfully. HeaderId={}",
                    header.getResultEntryId());
//            System.out.println("header = " + header);

            // Loop through validation list and update details
            for (ResultEntryValidationRequest validationReq : request.getValidationList()) {
                log.debug("Processing detailId={}",
                        validationReq.getResultEntryDetailsId());
                Optional<DgResultEntryDetail> optionalDetail = dgResultEntryDetailRepository.findById(validationReq.getResultEntryDetailsId());
                if (optionalDetail.isEmpty()) continue;

                DgResultEntryDetail detail = optionalDetail.get();

                // Update result and remarks
                detail.setResult(validationReq.getResult());
                detail.setRemarks(validationReq.getRemarks());
                if(AppConstants.STATUS_F.equalsIgnoreCase(validationReq.getComparisonType())){
                    detail.setFixedId(dgFixedValueRepository.findById(validationReq.getFixedId()).orElse(null));
                }

                // Set validated status
                if (Boolean.TRUE.equals(validationReq.getValidated())) {
                    detail.setValidated(AppConstants.STATUS_Y.toLowerCase());
                }

                //save order status in dgOrderDt
                DgOrderDt dgOrderDt = labOrderDtRepository.findByOrderhdId_IdAndInvestigationId_InvestigationId(header.getOrderHd().getId(), detail.getInvestigationId().getInvestigationId());
                DgOrderDt byId = labOrderDtRepository.findById(dgOrderDt.getId()).orElseThrow(() -> new RuntimeException(AppConstants.LAB_ORDER_DETAIL_NOT_FOUND_ERR_MSG));
                byId.setOrderTrackingStatus(labOrderTrackingStatusRepository.findById(resultValidatedStatusId).orElseThrow());
                labOrderDtRepository.save(byId);

                // Save each detail
                LabTurnAroundTime labTurnAroundTime=labTurnAroundTimeRepository.findByOrderHd_IdAndInvestigation_InvestigationIdAndPatient_IdAndIsReject(header.getOrderHd().getId(),detail.getInvestigationId().getInvestigationId(),header.getHinId().getId(),false);
                labTurnAroundTime.setResultValidatedBy(currentUser.getFirstName()+" "+currentUser.getMiddleName()+" "+currentUser.getLastName());
                labTurnAroundTime.setResultValidationTime(LocalDateTime.now());
                labTurnAroundTimeRepository.save(labTurnAroundTime);
                dgResultEntryDetailRepository.save(detail);
            }

            //Check if all details are validated
            List<DgResultEntryDetail> allDetails = dgResultEntryDetailRepository.findByResultEntryId(header);
            boolean allValidated = allDetails.stream()
                    .allMatch(d -> AppConstants.STATUS_Y.equalsIgnoreCase(d.getValidated()));

            // Update header if all details validated
            if (allValidated) {
                log.info("All details validated. Updating header status.");
                header.setResultStatus(AppConstants.STATUS_Y.toLowerCase()); // All validated
                // header.setVerified("y");
                header.setVerifiedOn(LocalDate.now());
                header.setVerifiedTime(LocalTime.now().format(formatter));
                header.setResultVerifiedBy(Math.toIntExact(currentUser.getUserId()));
                // header.setResultUpdatedBy(currentUser.getUsername());
                //  header.setUpdateOn(LocalDateTime.now());
                dgResultEntryHeaderRepository.save(header);
            }
            log.info("Result validation completed successfully. HeaderId={}",
                    request.getResultEntryHeaderId());

            return ResponseUtils.createSuccessResponse(
                    AppConstants.RESULT_VALIDATION_SUCCESS_MSG,
                    new TypeReference<String>() {
                    });

        } catch (Exception e) {
            log.error("Error while validating result entry. HeaderId={}",
                    request.getResultEntryHeaderId(), e);
           throw new RuntimeException(e);
        }
    }

    @Override
    public ApiResponse<Page<ResultEntryHeaderForUpdateResponse>> getResultHeaderForUpdate(Long hospitalId, String patientName, String patientMobileNo, int page, int size) {
        try {
            log.info("getResultHeaderForUpdate method started for hospitalId -{}",hospitalId);
            patientName = (patientName == null || patientName.trim().isEmpty())
                    ? null
                    : patientName;
            patientMobileNo=(patientMobileNo ==null || patientMobileNo.trim().isEmpty())?null:patientMobileNo;
            Pageable pageable = PageRequest.of(page
                    , size
//                    , Sort.by(Sort.Direction.DESC,"orderHd.orderDate")
            );
            Page<ResultEntryHeaderForUpdateResponse> response =
                    dgResultEntryHeaderRepository.getResultHeaderForUpdate(
                            hospitalId,
                            patientName,
                            patientMobileNo,
                            pageable
                    ).map(p -> new ResultEntryHeaderForUpdateResponse(
                            p.getResultEntryId(),
                            p.getPatientName(),
                            p.getGenderName(),
                            p.getPatientAge(),
                            p.getPatientMobileNumber(),
                            p.getRelationName(),
                            p.getDoctorName(),
                            p.getOrderhdId(),
                            p.getOrderNo(),
                            p.getOrderDate(),
                            p.getOrderTime()
                    ));

            log.info("getResultHeaderForUpdate method ended for hospitalId -{}",hospitalId);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getResultForUpdate method error for hospitalId -{}",hospitalId, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<InvestigationsForResultUpdateResponse>> getInvestigationsForResultUpdate(Long orderHdId) {
        try {
            log.info("getInvestigationsForResultUpdate method started for resultEntryHeaderId={}", orderHdId);
            List<InvestigationsForResultUpdateResponse> list =
                    dgResultEntryDetailRepository.getInvestigationsForResultUpdateWrtOrderHd(orderHdId,AppConstants.STATUS_Y.toLowerCase());

            log.info("getInvestigationsForResultUpdate method ended for resultEntryHeaderId={}", orderHdId);

            return ResponseUtils.createSuccessResponse(list, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getInvestigationsForResultUpdate method error for resultEntryHeaderId={} :: ", orderHdId,e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<List<SubInvestigationsForResultValidationResponse>> getSubInvestigationsForResultUpdate(Long resultEntryDetailsId, Long investigationId) {
        try {
            log.info("getSubInvestigationsForResultUpdate method started for resultEntryDetailId={}", resultEntryDetailsId);

            List<SubInvestigationsForResultValidationResponse> list =
                    dgResultEntryDetailRepository.findSubInvestigationsByDetailId(resultEntryDetailsId,investigationId,AppConstants.STATUS_Y.toLowerCase());
            log.info("getSubInvestigationsForResultUpdate method started for resultEntryDetailId={}", resultEntryDetailsId);

            return ResponseUtils.createSuccessResponse(list, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getSubInvestigationsForResultUpdate method error for resultEntryDetailId={}", resultEntryDetailsId,e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }


    @Override
    @Transactional
    public ApiResponse<String> updateResult(ResultUpdateRequest request) {
        log.info("Starting updateResult process for HeaderId={}", request.getResultEntryHeaderId());
        try {
            User currentUser = authUtil.getCurrentUser();


            // Fetch header
            Optional<DgResultEntryHeader> optionalHeader = dgResultEntryHeaderRepository.findById(request.getResultEntryHeaderId());
            if (optionalHeader.isEmpty()) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        AppConstants.RESULT_ENTRY_HEADER_NOT_FOUND_ERR_MSG, HttpStatus.NOT_FOUND.value());
            }
            DgResultEntryHeader header = optionalHeader.get();
            log.info("Result Entry Header found. EntryId={}", header.getResultEntryId());
            // Update all details
            for (ResultUpdateDetailRequest detailReq : request.getResultUpdateDetailRequests()) {
                log.debug("Processing DetailId={}", detailReq.getResultEntryDetailsId());
                Optional<DgResultEntryDetail> optionalDetail = dgResultEntryDetailRepository.findById(detailReq.getResultEntryDetailsId());
                if (optionalDetail.isEmpty()) continue;
                DgResultEntryDetail detail = optionalDetail.get();
                if (!Objects.equals(detail.getResultEntryId().getResultEntryId(), header.getResultEntryId())) {
                    continue; // Skip if detail not under this header
                }

                //Update lab_result_amend_audit detail
                LabResultAmendAudit labResultAmendAudit= new LabResultAmendAudit();
                MasLabResultAmendmentType masLabResultAmendmentType = masLabResultAmendmentTypeRepository.findById(detailReq.getAmendmentTypeId()).orElseThrow(() -> new RuntimeException("Invalid Lab Result Amendment Type Id"));

                labResultAmendAudit.setPatient(header.getHinId());
                labResultAmendAudit.setAmendmentType(masLabResultAmendmentType);
                labResultAmendAudit.setAmendedBy(currentUser.getFirstName()+" "+currentUser.getMiddleName()+" "+currentUser.getLastName());
                labResultAmendAudit.setNewResult(detailReq.getResult());
                labResultAmendAudit.setOldResult(detailReq.getOldResult());
                labResultAmendAudit.setAmendedDatetime(LocalDateTime.now());
                labResultAmendAudit.setInvestigation(detail.getInvestigationId());
                labResultAmendAudit.setReasonForChange(masLabResultAmendmentType.getAmendmentTypeName());
                labResultAmendAudit.setGeneratedSampleId(detail.getGeneratedSampleId());
                labResultAmendAudit.setRemarks(detailReq.getRemarks());

                labResultAmendAuditRepository.save(labResultAmendAudit);
                log.info("Audit saved for DetailId={}", detail.getResultEntryDetailId());


                // Update result and remarks per detail
                detail.setResult(detailReq.getResult());
//                detail.setRemarks(detailReq.getRemarks());
                if(AppConstants.STATUS_F.equalsIgnoreCase(detailReq.getComparisonType())){
                    detail.setFixedId(dgFixedValueRepository.findById(detailReq.getFixedId()).orElse(null));
                }
                dgResultEntryDetailRepository.save(detail);
            }
            //  Update header audit fields
            header.setResultUpdatedBy(Math.toIntExact(currentUser.getUserId()));  // Who updated
            header.setUpdateOn(LocalDateTime.now());           // When updated
            dgResultEntryHeaderRepository.save(header);
            log.info("Result update completed successfully for HeaderId={}",
                    header.getResultEntryId());

            return ResponseUtils.createSuccessResponse(
                    "Result and remarks updated successfully", new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Exception occurred while updating result. HeaderId={}",
                    request.getResultEntryHeaderId(), e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public ApiResponse<Page<LabInvestigationsReportResponse>> getAllInvestigationsReport(
            Long hospitalId,
            String mobileNo,
            String patientName,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {
        try {
            log.info("getAllInvestigationsReport method started with mobileNo={}, patientName={}, fromDate={}, toDate={}", mobileNo, patientName, fromDate, toDate);

            mobileNo = (mobileNo == null || mobileNo.trim().isEmpty())
                    ? null
                    : "%" + mobileNo.trim() + "%";

            patientName = (patientName == null || patientName.trim().isEmpty())
                    ? null
                    : "%" + patientName.trim().toLowerCase() + "%";


            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "resultEntryId.resultDate", "resultEntryId.resultTime"));
            Page<LabInvestigationsReportResponse> response =
                    dgResultEntryDetailRepository.getLabInvestigationsReport(
                            hospitalId,
                            mobileNo,
                            patientName,
                            fromDate,
                            toDate,
                            AppConstants.STATUS_Y,
                            pageable
                    );

            log.info("getAllInvestigationsReport method ended with mobileNo={}, patientName={}, fromDate={}, toDate={}", mobileNo, patientName, fromDate, toDate);
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error occurred in getAllLabReports()", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Page<LabDetailedTATReportResponse>> getDetailedTatReports(
            Long hospitalId,
            Long investigationId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {
        try {
            log.info("getDetailedTatReports method started with investigationId={}, subChargeCodeId={}, fromDate={}, toDate={}", investigationId, subChargeCodeId, fromDate, toDate);

            if (fromDate == null || toDate == null) {
                throw new IllegalArgumentException("From Date and To Date are mandatory");
            }

            if (toDate.isBefore(fromDate)) {
                throw new IllegalArgumentException("To Date cannot be before From Date");
            }

            Pageable pageable = PageRequest.of(page, size, Sort.by("turnAroundTimeId").ascending());

            Page<LabDetailedTATReportResponse> result =
                    labTurnAroundTimeRepository.findTatReportWithPagination(
                            hospitalId,
                            investigationId,
                            subChargeCodeId,
                            fromDate,
                            toDate,
                            pageable
                    );

            log.info("getDetailedTatReports method ended with investigationId={}, subChargeCodeId={}, fromDate={}, toDate={}", investigationId, subChargeCodeId, fromDate, toDate);
            return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getDetailedTatReports() error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Page<LabSummaryTATReportResponse>> getSummaryTatReports(
            Long hospitalId,
            Long investigationId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {

        try {
            log.info("getSummaryTatReports method started with investigationId={}, subChargeCodeId={}, fromDate={}, toDate={}", investigationId, subChargeCodeId, fromDate, toDate);

            if (fromDate == null || toDate == null) {
                throw new IllegalArgumentException("From Date and To Date are mandatory");
            }

            if (toDate.isBefore(fromDate)) {
                throw new IllegalArgumentException("To Date cannot be before From Date");
            }

            Pageable pageable = PageRequest.of(page, size);

            Page<LabSummaryTATReportResponse> result =
                    labTurnAroundTimeRepository.getTatSummaryReport(
                            hospitalId,
                            investigationId,
                            subChargeCodeId,
                            fromDate,
                            toDate,
                            pageable
                    );

            log.info("getSummaryTatReports method ended with investigationId={}, subChargeCodeId={}, fromDate={}, toDate={}", investigationId, subChargeCodeId, fromDate, toDate);
            return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getSummaryTatReports() error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Page<LabAmenedAuditReportResponse>> getAmendAuditReports(
            Long hospitalId,
            String phnNum,
            String patientName,
            Long investigationId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {

        try {
            log.info("getAmendAuditReports method started with phnNum={}, patientName={}, investigationId={}, subChargeCodeId={}, fromDate={}, toDate={}", phnNum, patientName, investigationId, subChargeCodeId, fromDate, toDate);
            phnNum = (phnNum == null || phnNum.trim().isEmpty())
                    ? null
                    : "%" + phnNum.trim() + "%";

            patientName = (patientName == null || patientName.trim().isEmpty())
                    ? null
                    : "%" + patientName.trim().toLowerCase() + "%";
            Pageable pageable = PageRequest.of(
                    page,
                    size,
                    Sort.by("amendedDatetime").descending()
            );

            LocalDateTime fromDateTime = null;
            LocalDateTime toDateTime = null;

            if (fromDate != null && toDate != null) {
                fromDateTime = fromDate.atStartOfDay();
                toDateTime = toDate.atTime(23, 59, 59);
            }

            Page<LabAmenedAuditReportResponse> result =
                    labResultAmendAuditRepository.getAmendAuditReport(
                            hospitalId,
                            phnNum != null ? phnNum.trim() : null,
                            patientName != null ? patientName.trim() : null,
                            investigationId,
                            subChargeCodeId,
                            fromDateTime,
                            toDateTime,
                            pageable
                    );

            log.info("getAmendAuditReports method ended with phnNum={}, patientName={}, investigationId={}, subChargeCodeId={}, fromDate={}, toDate={}", phnNum, patientName, investigationId, subChargeCodeId, fromDate, toDate);
            return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("getAmendAuditReports() error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Page<OrderTrackingReportResponse>> getOrderTrackingReports(
            Long hospitalId,
            String patientName,
            String mobileNo,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {

        try {
            log.info("getOrderTrackingReports method started with patientName={}, mobileNo={}, fromDate={}, toDate={}", patientName, mobileNo, fromDate, toDate);
            log.info("getOrderTrackingReports() started...");

            mobileNo = (mobileNo == null || mobileNo.trim().isEmpty())
                    ? null
                    : "%" + mobileNo.trim() + "%";

            patientName = (patientName == null || patientName.trim().isEmpty())
                    ? null
                    : "%" + patientName.trim().toLowerCase() + "%";

            Pageable pageable = PageRequest.of(page, size, Sort.by("orderhdId.orderDate").descending());

            Page<OrderTrackingReportResponse> result =
                    labOrderDtRepository.getOrderTrackingReport(
                            hospitalId,
                            patientName,
                            mobileNo,
                            fromDate,
                            toDate,
                            pageable
                    );

            log.info("getOrderTrackingReports method ended with patientName={}, mobileNo={}, fromDate={}, toDate={}", patientName, mobileNo, fromDate, toDate);
            return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Error in getOrderTrackingReports", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Page<LabIncompleteInvestigationsReportResponse>> getIncompleteInvestigationReports(
            Long hospitalId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("orderhdId.orderDate").descending());

        List<Long> statuses = List.of(
                sampleCollectStatusId,
                sampleRejectStatusId,
                sampleValidateStatusId,
                resultEnteredStatusId
        );

        Page<LabIncompleteInvestigationsReportResponse> result =
                labOrderDtRepository.getIncompleteInvestigations(
                        hospitalId,subChargeCodeId, fromDate, toDate, statuses, pageable
                );

        return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});
    }

     @Override
     public ApiResponse<Page<SampleRejectionInvestigationReportResponse>> getSampleRejectionReport(
             Long hospitalId,
             Long subChargeCodeId,
             LocalDate fromDate,
             LocalDate toDate,
             int page,
             int size) {

         try {
             log.info("getSampleRejectionReport method started with subChargeCodeId={}, fromDate={}, toDate={}", subChargeCodeId, fromDate, toDate);

             Pageable pageable = PageRequest.of(page, size);

             Page<SampleRejectionInvestigationReportResponse> result =
                     dgSampleCollectionDetailsRepository.getRejectedInvestigations(
                             hospitalId,subChargeCodeId, fromDate, toDate, pageable
                     );

             log.info("getSampleRejectionReport method ended with subChargeCodeId={}, fromDate={}, toDate={}", subChargeCodeId, fromDate, toDate);
             return ResponseUtils.createSuccessResponse(result, new TypeReference<>() {});

         } catch (Exception e) {
             log.error("Error occurred in getSampleRejectionReport()", e);
             return ResponseUtils.createFailureResponse(
                     null,
                     new TypeReference<>() {},
                     "Internal Server Error",
                     HttpStatus.INTERNAL_SERVER_ERROR.value()
             );
         }
     }


    public String generateSampleId(
            String modalityCode,      // "BIO"
            int dailySequence,        //  1 → 0001
            String containerSuffix      // CS
    ) {

        String datePart = LocalDate.now()
                .format(DateTimeFormatter.BASIC_ISO_DATE); // yyyyMMdd

        String sequencePart = String.format("%04d", dailySequence);

        return modalityCode + "-" +
                datePart + "-" +
                sequencePart + "-" +
                containerSuffix;
    }
    private Long extractAgeFromString(String age) {
        return Long.parseLong(age.substring(0, age.indexOf("Y")).trim());
    }
}
