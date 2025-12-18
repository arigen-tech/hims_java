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


    private String getCurrentTimeFormatted(Instant instant) {
        LocalTime time = instant
                .atZone(ZoneId.systemDefault())
                .toLocalTime();

        return time.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }


    @Override
    @Transactional
    public ApiResponse<String> validateInvestigations(List<InvestigationValidationRequest> requests) {
        try {
            log.info("Investigation validation process started...");

            // 0) current user check
            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            Long headerId = requests.get(0).getSampleHeaderId();

            // 1) LOOP ALL DETAILS
            for (InvestigationValidationRequest req : requests) {

                // fetch details
                DgSampleCollectionDetails details =
                        detailsRepo.findById(req.getDetailId())
                                .orElseThrow(() -> new RuntimeException("Details not found"));

                DgSampleCollectionHeader header = details.getSampleCollectionHeader();
                Long investigationId = details.getInvestigationId().getInvestigationId();
                boolean accepted = Boolean.TRUE.equals(req.getAccepted());

                // 2) UPDATE SAMPLE COLLECTION DETAILS

                int orderHdId = header.getVisitId().getBillingHd().getHdorder().getId();

                LabTurnAroundTime labTurnAroundTime = labTurnAroundTimeRepository.findByOrderHd_IdAndInvestigation_InvestigationIdAndPatient_Id(orderHdId, investigationId, header.getPatientId().getId());
                String detailStatus;
                if(accepted){
                    detailStatus="y";
                    labTurnAroundTime.setIsReject(false);
                }else{
                    detailStatus="r";
                    labTurnAroundTime.setIsReject(true);
                }
//                String detailStatus = accepted ? "y" : "r";
                detailsRepo.updateValidation(details.getSampleCollectionDetailsId(), detailStatus);

                // 🔥 VERY IMPORTANT FIX — to ensure header sees updated statuses
//                entityManager.flush();
//                entityManager.clear();

                // set entity fields also
                details.setValidated(detailStatus);

                if (!accepted) {
                    details.setRejected_reason(req.getReason());
                    details.setOldSampleCollectionHdIdForReject(headerId);
                } else {
                    details.setRejected_reason(null);
                    details.setOldSampleCollectionHdIdForReject(null);
                }




                labTurnAroundTime.setSampleValidatedBy(currentUser.getFirstName()+" "+currentUser.getMiddleName()+" "+currentUser.getLastName());
                labTurnAroundTime.setSampleValidatedDateTime(LocalDateTime.now());
                labTurnAroundTimeRepository.save(labTurnAroundTime);


                detailsRepo.save(details);

                // 3) IF REJECTED → UPDATE ORDERDT
                DgOrderHd orderHd = orderHdRepo.findByPatientId_IdAndVisitId_Id(
                        header.getPatientId().getId(),
                        header.getVisitId().getId()
                );

                if (orderHd != null) {
                    DgOrderDt orderDt =
                            orderDtRepo.findByOrderhdId_IdAndInvestigationId_InvestigationId(
                                    orderHd.getId(),
                                    investigationId
                            );

                    if (orderDt != null) {
                        String orderDtStatus = accepted ? "y" : "n";
                        orderDtRepo.updateOrderStatus((long) orderDt.getId(), orderDtStatus);

                        // update entity
                        orderDt.setOrderStatus(orderDtStatus);
                        orderDtRepo.save(orderDt);

                        log.info("OrderDt {} -> {}", orderDt.getId(), orderDtStatus);
                    }
                }
            } // end loop

            // 4) UPDATE HEADER VALIDATION STATUS
            List<String> headerStatuses = detailsRepo.getValidationStatusOfHeader(headerId);

            boolean allAccepted = headerStatuses.stream().allMatch(s -> s.equals("y"));
            boolean allRejected = headerStatuses.stream().allMatch(s -> s.equals("r"));



            String finalHeaderStatus =
                    allAccepted ? "y" :
                            allRejected ? "r" :
                                    "y"; // partial = y

//            int i = headerRepo.updateValidationStatus(headerId, finalHeaderStatus);

//            log.info("Header Validation Update = {}", i);

            // 5) SET HEADER VALIDATION DATE + VALIDATED BY
            DgSampleCollectionHeader header =
                    headerRepo.findById(headerId).orElseThrow();

            header.setValidated(finalHeaderStatus);

            header.setValidation_date(LocalDate.now());
            header.setValidationTime(Instant.now());
            header.setValidatedBy(currentUser.getFirstName()+" "+currentUser.getMiddleName()+" "+currentUser.getLastName());


            //LabTurnAroundTime Set

            LabTurnAroundTime labTurnAroundTime= new LabTurnAroundTime();
            labTurnAroundTime.setSampleValidatedBy(currentUser.getFirstName()+" "+currentUser);

            headerRepo.save(header);



            // 6) UPDATE ORDERHD STATUS
            DgOrderHd orderHd =
                    orderHdRepo.findByPatientId_IdAndVisitId_Id(
                            header.getPatientId().getId(),
                            header.getVisitId().getId()
                    );

            if (orderHd != null) {

                List<String> orderDtStatuses =
                        orderDtRepo.getOrderStatusesOfOrderHd((long) orderHd.getId());

                boolean allOrderDtRejected =
                        orderDtStatuses.stream().allMatch(s -> s.equals("n"));

                boolean allOrderDtAccepted =
                        orderDtStatuses.stream().allMatch(s -> s.equals("y"));

                String finalOrderStatus;
                if (allOrderDtRejected) {
                    finalOrderStatus = "n";
                } else if (allOrderDtAccepted) {
                    finalOrderStatus = "y";
                } else {
                    finalOrderStatus = "p";
                }

                orderHdRepo.updateOrderStatus((long) orderHd.getId(), finalOrderStatus);

                orderHd.setOrderStatus(finalOrderStatus);
                orderHdRepo.save(orderHd);

                log.info("OrderHd {} Updated to {}", orderHd.getId(), finalOrderStatus);
            }

            return ResponseUtils.createSuccessResponse(
                    "Investigation validated successfully",
                    new TypeReference<String>() {});
        }
        catch (Exception e) {
            log.error("Sample Validate Error :: ", e);
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Internal Server Error", HttpStatus.BAD_REQUEST.value());
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
                                        d.getInvestigationId() != null && d.getInvestigationId().getSampleId() != null
                                                ? d.getInvestigationId().getSampleId().getSampleCode()
                                                : null,
                                        d.getInvestigationId() != null ? d.getInvestigationId().getInvestigationName() : null,
                                        d.getSampleId() != null ? d.getSampleId().getId() : null,
                                        d.getSampleId() != null ? d.getSampleId().getSampleDescription() : null,
                                        d.getQuantity(),
                                        d.getEmpanelledStatus(),
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
                                patient.getUhidNo(),
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
            log.info("Fetching current user");

            User currentUser = authUtil.getCurrentUser();
            if (currentUser == null) {
                log.warn("Unauthorized access attempt: current user not found");
                return ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        "Current user not found", HttpStatus.UNAUTHORIZED.value());
            }

            log.info("Fetching sample collection details for result validation");

            List<DgSampleCollectionDetails> detailsList =
                    detailsRepo.findAllByHeaderResultEntryAndValidationStatusLogic();

            log.debug("Total sample collection details found: {}", detailsList.size());

            Map<String, ResultResponse> responseMap = new LinkedHashMap<>();

            for (DgSampleCollectionDetails detail : detailsList) {

                DgSampleCollectionHeader header = detail.getSampleCollectionHeader();
                Long headerId = header.getSampleCollectionHeaderId();
                String key = String.valueOf(headerId);

                log.debug("Processing SampleCollectionHeaderId: {}", headerId);

                ResultResponse response = responseMap.computeIfAbsent(key, k -> {
                    ResultResponse r = new ResultResponse();

                    var patient = header.getPatientId();
                    log.debug("Preparing response for PatientId: {}", patient.getId());

                    String fullName = Stream.of(
                                    patient.getPatientFn(),
                                    patient.getPatientMn(),
                                    patient.getPatientLn()
                            ).filter(Objects::nonNull)
                            .filter(s -> !s.isBlank())
                            .collect(Collectors.joining(" "));

                    r.setPatientId(patient.getId());
                    r.setPatientName(fullName);
                    r.setPatientGender(patient.getPatientGender() != null
                            ? patient.getPatientGender().getGenderName() : null);
                    r.setPatientAge(patient.getPatientAge());
                    r.setPatientPhoneNo(patient.getPatientMobileNumber());

                    DgOrderHd dgOrderHd = labHdRepository.findByVisitId(header.getVisitId());
                    r.setOrderDate(String.valueOf(dgOrderHd.getOrderDate()));
                    r.setOrderTime(getCurrentTimeFormatted(dgOrderHd.getOrderTime()));

                    r.setCollectedBy(header.getCollection_by());
                    r.setValidatedBy(header.getValidatedBy());
                    r.setValidatedDate(header.getValidation_date());
                    r.setValidatedTime(header.getValidationTime() != null
                            ? getCurrentTimeFormatted(header.getValidationTime()) : null);

                    r.setSampleCollectionHeaderId(headerId);
                    r.setSubChargeCodeId(header.getSubChargeCode().getSubId());
                    r.setSubChargeCodeName(header.getSubChargeCode().getSubName());
                    r.setResultInvestigationResponseList(new ArrayList<>());

                    return r;
                });

                ResultInvestigationResponse investigation =
                        response.getResultInvestigationResponseList().stream()
                                .filter(i -> i.getInvestigationId()
                                        .equals(detail.getInvestigationId().getInvestigationId()))
                                .findFirst()
                                .orElseGet(() -> {
                                    DgMasInvestigation invObj = detail.getInvestigationId();
                                    log.debug("Creating investigation response for InvestigationId: {}",
                                            invObj.getInvestigationId());

                                    ResultInvestigationResponse inv = new ResultInvestigationResponse();
                                    inv.setInvestigationId(invObj.getInvestigationId());
                                    inv.setInvestigationName(invObj.getInvestigationName());
                                    inv.setResultType(invObj.getInvestigationType());
                                    inv.setResultSubInvestigationResponseList(new ArrayList<>());

                                    response.getResultInvestigationResponseList().add(inv);
                                    return inv;
                                });

                log.debug("Fetching sub-investigations for InvestigationId: {}",
                        detail.getInvestigationId().getInvestigationId());

                List<DgSubMasInvestigation> subList =
                        dgSubMasInvestigationRepository
                                .findByInvestigationId(detail.getInvestigationId().getInvestigationId());

                for (DgSubMasInvestigation subInvest : subList) {
                    ResultSubInvestigationResponse sub = new ResultSubInvestigationResponse();
                    sub.setSubInvestigationId(subInvest.getSubInvestigationId());
                    sub.setSubInvestigationName(subInvest.getSubInvestigationName());

                    investigation.getResultSubInvestigationResponseList().add(sub);
                }
            }

            log.info("Result investigation data prepared successfully");
            return ResponseUtils.createSuccessResponse(
                    new ArrayList<>(responseMap.values()),
                    new TypeReference<>() {}
            );

        } catch (Exception e) {
            log.error("Error while fetching investigation status", e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    "Internal Server Error",
                    HttpStatus.BAD_REQUEST.value()
            );
        }
    }

}



