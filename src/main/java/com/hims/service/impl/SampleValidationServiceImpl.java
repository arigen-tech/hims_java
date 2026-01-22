package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.InvestigationValidationRequest;
import com.hims.response.*;
import com.hims.service.SampleValidationService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class SampleValidationServiceImpl implements SampleValidationService {
    @Autowired
     DgSampleCollectionDetailsRepository detailsRepo;
    @Autowired
    DgSampleCollectionHeaderRepository headerRepo;
    @Autowired
    DgSubMasInvestigationRepository dgSubMasInvestigationRepository;
@Autowired
DgNormalValueRepository dgNormalValueRepository;
@Autowired
    VisitRepository visitRepository;
@Autowired
    LabHdRepository labHdRepository;
@Autowired
DgFixedValueRepository dgFixedValueRepository;

@Autowired
private LabTurnAroundTimeRepository labTurnAroundTimeRepository;
    @Autowired
    AuthUtil authUtil;

    @Autowired
    private MasSubChargeCodeRepository subChargeCodeRepository;

    @Autowired
    private LabDtRepository orderDtRepo;

    @Autowired
    private  LabHdRepository orderHdRepo;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private LabOrderTrackingStatusRepository labOrderTrackingStatusRepository;

    @Value("${lab.track-order-status-sample.validate}")
    private Long validatedStatusId;

    @Value("${lab.track-order-status-sample.reject}")
    private Long rejectedStatusId;


    private String getCurrentTimeFormatted(Instant instant) {
        LocalTime time = instant
                .atZone(ZoneId.systemDefault())
                .toLocalTime();

        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }


    @Override
    @Transactional
    public ApiResponse<String> validateInvestigations(
            List<InvestigationValidationRequest> requests) {

        try {
            log.info("Investigation validation process started...");

            // ===================== 0. CURRENT USER =====================
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<>() {},
                        "Current user not found",
                        HttpStatus.UNAUTHORIZED.value());
            }

            String validatedBy =
                    currentUser.getFirstName() + " " +
                            currentUser.getMiddleName() + " " +
                            currentUser.getLastName();

            Long headerId = requests.get(0).getSampleHeaderId();

            // ===================== 1. FETCH HEADER ONCE =====================
            DgSampleCollectionHeader header =
                    headerRepo.findById(headerId)
                            .orElseThrow(() -> new RuntimeException("Header not found"));

            // ===================== 2. FETCH ORDER HD ONCE =====================
            DgOrderHd orderHd =
                    orderHdRepo.findByPatientId_IdAndVisitId_Id(
                            header.getPatientId().getId(),
                            header.getVisitId().getId()
                    );

            // ===================== 3. CACHE TRACKING STATUS =====================
            LabOrderTrackingStatus validatedStatus =
                    labOrderTrackingStatusRepository
                            .findById(validatedStatusId).orElseThrow();

            LabOrderTrackingStatus rejectedStatus =
                    labOrderTrackingStatusRepository
                            .findById(rejectedStatusId).orElseThrow();

            // ===================== 4. LOOP ALL REQUESTS =====================
            for (InvestigationValidationRequest req : requests) {

                DgSampleCollectionDetails details =
                        detailsRepo.findById(req.getDetailId())
                                .orElseThrow(() ->
                                        new RuntimeException("Details not found"));

                boolean accepted = Boolean.TRUE.equals(req.getAccepted());
                String detailStatus = accepted ? "y" : "r";

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

                // ===================== 6. UPDATE SAMPLE DETAILS =====================
                detailsRepo.updateValidation(
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

                detailsRepo.save(details);

                // ===================== 7. UPDATE ORDER DT =====================
                if (orderHd != null) {

                    DgOrderDt orderDt =
                            orderDtRepo
                                    .findByOrderhdId_IdAndInvestigationId_InvestigationId(
                                            orderHd.getId(),
                                            investigationId
                                    );

                    if (orderDt != null) {

                        String orderDtStatus = accepted ? "y" : "n";

                        orderDtRepo.updateOrderStatus(
                                (long) orderDt.getId(),
                                orderDtStatus
                        );

                        orderDt.setOrderStatus(orderDtStatus);
                        orderDt.setOrderTrackingStatus(
                                accepted ? validatedStatus : rejectedStatus
                        );

                        orderDtRepo.save(orderDt);
                    }
                }
            }

            // ===================== 8. UPDATE HEADER STATUS =====================
            List<String> headerStatuses =
                    detailsRepo.getValidationStatusOfHeader(headerId);

            boolean allAccepted =
                    headerStatuses.stream().allMatch("y"::equals);

            boolean allRejected =
                    headerStatuses.stream().allMatch("r"::equals);

            String finalHeaderStatus =
                    allAccepted ? "y" :
                            allRejected ? "r" : "y"; // partial = y

            header.setValidated(finalHeaderStatus);
            header.setValidation_date(LocalDate.now());
            header.setValidationTime(Instant.now());
            header.setValidatedBy(validatedBy);
            headerRepo.save(header);

            // ===================== 9. UPDATE ORDER HD =====================
            if (orderHd != null) {

                List<String> orderDtStatuses =
                        orderDtRepo.getOrderStatusesOfOrderHd(
                                (long) orderHd.getId()
                        );

                boolean allOrderRejected =
                        orderDtStatuses.stream().allMatch("n"::equals);

                boolean allOrderAccepted =
                        orderDtStatuses.stream().allMatch("y"::equals);

                String finalOrderStatus =
                        allOrderRejected ? "n" :
                                allOrderAccepted ? "y" : "p";

                orderHdRepo.updateOrderStatus(
                        (long) orderHd.getId(),
                        finalOrderStatus
                );

                orderHd.setOrderStatus(finalOrderStatus);
                orderHdRepo.save(orderHd);
            }

            // ===================== 10. SUCCESS =====================
            return ResponseUtils.createSuccessResponse(
                    "Investigation validated successfully",
                    new TypeReference<String>() {}
            );

        } catch (Exception e) {

            log.error("Sample Validate Error :: ", e);

            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    @Override
    @Transactional
    public ApiResponse<List<SampleValidationResponse>> getInvestigationsWithOrderStatusNAndP() {
        try {
            log.info("Investigation status process started..");

            // Step 1: Fetch all valid details with joins
            List<DgSampleCollectionDetails> detailsList = detailsRepo.findAllByHeaderValidatedStatusLogic();
            log.info("Fetched details count: {}", detailsList.size());

            // Step 2: Group by (patientId + headerId)
            Map<String, List<DgSampleCollectionDetails>> grouped = detailsList.stream()
                    .filter(d -> d.getSampleCollectionHeader() != null && d.getSampleCollectionHeader().getPatientId() != null)
                    .collect(Collectors.groupingBy(d -> {
                        DgSampleCollectionHeader h = d.getSampleCollectionHeader();
                        return h.getPatientId().getId() + "_" + h.getSampleCollectionHeaderId();
                    }));

            // Step 3: Convert each group into SampleValidationResponse
            List<SampleValidationResponse> responseList = grouped.entrySet().stream()
                    .map(entry -> {
                        List<DgSampleCollectionDetails> groupDetails = entry.getValue();
                        DgSampleCollectionHeader header = groupDetails.get(0).getSampleCollectionHeader();

                        var patient = header.getPatientId();
                        var subCharge = header.getSubChargeCode();
                        DgOrderHd orderHd = labHdRepository.findByPatientId_IdAndVisitId_Id(patient.getId(),header.getVisitId().getId());

                        String fullName = Stream.of(
                                        patient.getPatientFn(),
                                        patient.getPatientMn(),
                                        patient.getPatientLn()
                                )
                                .filter(Objects::nonNull)
                                .filter(s -> !s.isBlank())
                                .collect(Collectors.joining(" "));

                        // Map details to TestDetailsDTO
                        List<TestDetailsDTO> investigations = groupDetails.stream()
                                .map(d -> new TestDetailsDTO(
                                        d.getSampleCollectionDetailsId(),
                                        d.getInvestigationId() != null ? d.getInvestigationId().getInvestigationId() : null,
                                        d.getSampleGeneratedId() != null ? d.getSampleGeneratedId(): null,
                                        d.getInvestigationId() != null ? d.getInvestigationId().getInvestigationName() : null,
                                        d.getSampleId() != null ? d.getSampleId().getId() : null,
                                        d.getSampleId() != null ? d.getSampleId().getSampleDescription() : null,
                                        d.getInvestigationId() != null?d.getInvestigationId().getQuantity():null,

                                        d.getInvestigationId() != null ? d.getInvestigationId().getCollectionId().getCollectionId():null,
                                        d.getInvestigationId() != null ? d.getInvestigationId().getCollectionId().getCollectionName():null,                                        d.getEmpanelledStatus(),
                                        d.getSampleCollDatetime(),
                                        d.getRejected_reason(),
                                        d.getRemarks()
                                ))
                                .toList();
                        // Build final response (each header = separate entry)
                        return new SampleValidationResponse(
                                header.getSampleCollectionHeaderId(),
                                patient.getId(),
                                fullName,
                                patient.getPatientGender() != null ? patient.getPatientGender().getGenderName() : null,
                                patient.getPatientAge(),
                                patient.getPatientMobileNumber(),
                                subCharge != null ? subCharge.getSubId() : null,
                                subCharge != null ? subCharge.getSubName() : null,
                                orderHd.getOrderNo(),
                                header.getLastChgDate() != null ? header.getLastChgDate().toLocalDate() : null,
                                header.getCollection_time(),
                                header.getCollection_by(),
                                patient.getPatientRelation() != null ? patient.getPatientRelation().getRelationName() : null,
                                investigations
                        );
                    })
                    .toList();

            log.info("Investigation status process ended..");
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

        } catch (Exception e) {
            log.error("Investigation status Error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {}, "Internal Server Error", HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    @Override
    public ApiResponse<List<ResultResponse>> getValidatedResultEntries() {

        try {
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            List<DgSampleCollectionDetails> detailsList =
                    detailsRepo.findAllByHeaderResultEntryAndValidationStatusLogic();

            // ===================== CACHE MAPS =====================
            Map<Long, DgOrderHd> orderHdCache = new HashMap<>();
            Map<Long, MasSubChargeCode> subChargeCache = new HashMap<>();
            Map<Long, List<DgSubMasInvestigation>> subInvestigationCache = new HashMap<>();
            Map<Long, List<DgFixedValue>> fixedValueCache = new HashMap<>();
            Map<String, DgNormalValue> normalValueCache = new HashMap<>();

            Map<String, ResultResponse> responseMap = new LinkedHashMap<>();

            for (DgSampleCollectionDetails detail : detailsList) {

                DgSampleCollectionHeader header = detail.getSampleCollectionHeader();
                Long headerId = header.getSampleCollectionHeaderId();
                String key = String.valueOf(headerId);

                // ===================== HEADER GROUP =====================
                ResultResponse response = responseMap.computeIfAbsent(key, k -> {

                    ResultResponse r = new ResultResponse();
                    var patient = header.getPatientId();

                    String fullName = Stream.of(
                                    patient.getPatientFn(),
                                    patient.getPatientMn(),
                                    patient.getPatientLn()
                            )
                            .filter(Objects::nonNull)
                            .filter(s -> !s.isBlank())
                            .collect(Collectors.joining(" "));

                    r.setPatientId(patient.getId());
                    r.setPatientName(fullName);
                    r.setRelation(patient.getPatientRelation() != null ? patient.getPatientRelation().getRelationName() : null);
                    r.setRelationId(patient.getPatientRelation() != null ? patient.getPatientRelation().getId() : null);
                    r.setPatientGender(patient.getPatientGender() != null ? patient.getPatientGender().getGenderName() : null);
                    r.setPatientAge(patient.getPatientAge());
                    r.setPatientPhoneNo(patient.getPatientMobileNumber());

                    // -------- Order HD (CACHED) --------
                    DgOrderHd orderHd = orderHdCache.computeIfAbsent(
                            header.getVisitId().getId(),
                            id -> labHdRepository.findByVisitId(header.getVisitId())
                    );

                    r.setOrderDate(String.valueOf(orderHd.getOrderDate()));
                    r.setOrderTime(getCurrentTimeFormatted(orderHd.getOrderTime()));
                    r.setOrderNo(orderHd.getOrderNo());

                    r.setCollectedBy(header.getCollection_by());
                    r.setValidatedBy(header.getValidatedBy());
                    r.setValidatedDate(header.getValidation_date());
                    r.setValidatedTime(
                            header.getValidationTime() != null
                                    ? getCurrentTimeFormatted(header.getValidationTime())
                                    : null
                    );
                    r.setCollectedDate(header.getCollection_time());
                    r.setCollectedTime(
                            header.getCollection_time() != null
                                    ? header.getCollection_time().toLocalTime()
                                    : null
                    );

                    r.setDepartment(
                            header.getDepartmentId() != null
                                    ? header.getDepartmentId().getDepartmentName()
                                    : null
                    );

                    // -------- Sub Charge Code (CACHED) --------
                    MasSubChargeCode subChargeCode =
                            subChargeCache.computeIfAbsent(
                                    header.getSubChargeCode().getSubId(),
                                    id -> subChargeCodeRepository.findById(id).orElseThrow()
                            );

                    r.setMainChargeCodeId(subChargeCode.getMainChargeId().getChargecodeId());
                    r.setSubChargeCodeId(subChargeCode.getSubId());
                    r.setSubChargeCodeName(subChargeCode.getSubName());

                    r.setDoctorName(
                            header.getHospitalId() != null
                                    ? header.getHospitalId().getHospitalName()
                                    : null
                    );

                    r.setVisitId(header.getVisitId().getId());
                    r.setSampleCollectionHeaderId(headerId);
                    r.setResultInvestigationResponseList(new ArrayList<>());
                    return r;
                });

                // ===================== INVESTIGATION GROUP =====================
                ResultInvestigationResponse investigation =
                        response.getResultInvestigationResponseList()
                                .stream()
                                .filter(i -> i.getInvestigationId()
                                        .equals(detail.getInvestigationId().getInvestigationId()))
                                .findFirst()
                                .orElseGet(() -> {

                                    DgMasInvestigation invObj = detail.getInvestigationId();
                                    ResultInvestigationResponse inv = new ResultInvestigationResponse();

                                    inv.setInvestigationId(invObj.getInvestigationId());
                                    inv.setInvestigationName(invObj.getInvestigationName());
                                    inv.setSampleCollectionDetailsId(detail.getSampleCollectionDetailsId());
                                    inv.setResultType(invObj.getInvestigationType());
                                    inv.setGeneratedSampleId(detail.getSampleGeneratedId());

                                    if (invObj.getSampleId() != null) {
                                        inv.setSampleId(invObj.getSampleId().getId());
                                        inv.setSampleName(invObj.getSampleId().getSampleDescription());
                                    }

                                    if (invObj.getUomId() != null) {
                                        inv.setUnitId(invObj.getUomId().getId());
                                        inv.setUnitName(invObj.getUomId().getName());
                                    }

                                    String normalRange = invObj.getNormalValue();
                                    if ((normalRange == null || normalRange.isBlank())
                                            && invObj.getMinNormalValue() != null
                                            && invObj.getMaxNormalValue() != null) {
                                        normalRange =
                                                invObj.getMinNormalValue() + " - " + invObj.getMaxNormalValue();
                                    }

                                    inv.setNormalValue(normalRange);
                                    inv.setResultSubInvestigationResponseList(new ArrayList<>());

                                    response.getResultInvestigationResponseList().add(inv);
                                    return inv;
                                });

                // ===================== SUB INVESTIGATIONS (CACHED) =====================
                List<DgSubMasInvestigation> subList =
                        subInvestigationCache.computeIfAbsent(
                                detail.getInvestigationId().getInvestigationId(),
                                id -> dgSubMasInvestigationRepository.findByInvestigationId(id)
                        );

                for (DgSubMasInvestigation subInvest : subList) {

                    ResultSubInvestigationResponse sub = new ResultSubInvestigationResponse();
                    sub.setSubInvestigationId(subInvest.getSubInvestigationId());
                    sub.setSubInvestigationName(subInvest.getSubInvestigationName());
                    sub.setSampleId(subInvest.getSampleId() != null ? subInvest.getSampleId().getId() : null);
                    sub.setSampleName(subInvest.getSampleId() != null ? subInvest.getSampleId().getSampleDescription() : null);
                    sub.setUnit(subInvest.getUomId() != null ? subInvest.getUomId().getName() : null);
                    sub.setComparisonType(subInvest.getComparisonType());
                    sub.setResultType(subInvest.getResultType());
                    sub.setGeneratedSampleId(detail.getSampleGeneratedId());

                    // -------- Normal Value (CACHED) --------
                    var patient = header.getPatientId();
                    String gender = patient.getPatientGender() != null
                            ? patient.getPatientGender().getGenderCode()
                            : null;
                    String ageStr = patient.getPatientAge();

                    Long ageInYears = null;
                    if (ageStr != null && ageStr.matches("\\d+Y.*")) {
                        try {
                            ageInYears = Long.parseLong(ageStr.substring(0, ageStr.indexOf("Y")).trim());
                        } catch (Exception ignored) {}
                    }

                    String normalKey =
                            subInvest.getSubInvestigationId() + "|" + gender + "|" + ageInYears;

                    Long finalAgeInYears = ageInYears;
                    Long finalAgeInYears1 = ageInYears;
                    DgNormalValue dgNormalValue =
                            normalValueCache.computeIfAbsent(normalKey, k -> {
                                if (finalAgeInYears != null && gender != null) {
                                    return dgNormalValueRepository
                                            .findFirstBySubInvestigationIdAndSexAndFromAgeLessThanEqualAndToAgeGreaterThanEqual(
                                                    subInvest,
                                                    gender.substring(0, 1).toUpperCase(),
                                                    finalAgeInYears,
                                                    finalAgeInYears1
                                            );
                                }
                                return dgNormalValueRepository.findBySubInvestigationId(subInvest);
                            });

                    if (dgNormalValue != null) {
                        String normalRange = dgNormalValue.getNormalValue();
                        if ((normalRange == null || normalRange.isBlank())
                                && dgNormalValue.getMinNormalValue() != null
                                && dgNormalValue.getMaxNormalValue() != null) {
                            normalRange =
                                    dgNormalValue.getMinNormalValue() + " - " +
                                            dgNormalValue.getMaxNormalValue();
                        }
                        sub.setNormalValue(normalRange);
                        sub.setNormalId(dgNormalValue.getNormalId());
                    }

                    // -------- Fixed Values (CACHED) --------
                    List<DgFixedValue> fixedValues =
                            fixedValueCache.computeIfAbsent(
                                    subInvest.getSubInvestigationId(),
                                    id -> dgFixedValueRepository.findBySubInvestigationId(subInvest)
                            );

                    List<DgFixedValueResponse> fixedResponses = new ArrayList<>();
                    for (DgFixedValue fv : fixedValues) {
                        DgFixedValueResponse fr = new DgFixedValueResponse();
                        fr.setFixedId(fv.getFixedId());
                        fr.setFixedValue(fv.getFixedValue());
                        fr.setSubInvestigationId(subInvest.getSubInvestigationId());
                        fixedResponses.add(fr);
                    }

                    sub.setDgFixedValueResponseList(fixedResponses);
                    sub.setFixedValueExpectedResult(subInvest.getFixedValueExpectedValue());

                    investigation.getResultSubInvestigationResponseList().add(sub);
                }
            }

            return ResponseUtils.createSuccessResponse(
                    new ArrayList<>(responseMap.values()),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Investigation status Error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.BAD_REQUEST.value());
        }
    }

}



