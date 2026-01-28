package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.response.*;
import com.hims.service.LabReportService;
import com.hims.utils.ResponseUtils;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabReportServiceImpl implements LabReportService {

    private final DgResultEntryDetailRepository resultEntryDetailRepository;

    private final UserRepo userRepo;

    private final LabTurnAroundTimeRepository labTurnAroundTimeRepository;

    private final LabResultAmendAuditRepository amendAuditRepository;

    private final DgSampleCollectionDetailsRepository dgSampleCollectionDetailsRepository;

    private final LabDtRepository dgOrderDtRepository;

    @Value("${lab.track-order-status-sample.collect}")
    private Long sampleCollectStatusId;

    @Value("${lab.track-order-status-sample.validate}")
    private Long sampleValidateStatusId;

    @Value("${lab.track-order-status-sample.reject}")
    private Long sampleRejectStatusId;

    @Value("${lab.track-order-status-result.entry}")
    private Long resultEnteredStatusId;



    @Override
    public ApiResponse<List<AllLabReportResponse>> getAllLabReports(String phnNum, String patientName, LocalDate fromDate,LocalDate toDate) {
        try {
            log.info("getAllLabReports() Started..." );
//            if (fromDate == null || toDate == null) {
//                throw new IllegalArgumentException("From Date and To Date are mandatory");
//            }

//            Sort sort=Sort.by(Sort.Direction.DESC,"resultEntryId.resultDate");
            Specification<DgResultEntryDetail> spec =
                    filterLabReports(
                            phnNum,
                            patientName,
                            fromDate,
                            toDate
                    );
            List<DgResultEntryDetail> details =
                    resultEntryDetailRepository.findAll(spec);
            log.info("getAllLabReports() Ended..." );
            return  ResponseUtils.createSuccessResponse(details.stream().map(this::mapToResponse).toList(), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Error occurred in :: getAllLabReports()",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    @Override
    public ApiResponse<List<LabDetailedTATReportResponse>> getDetailedTatReports(Long investigationId, Long subChargeCodeId, LocalDate fromDate, LocalDate toDate) {
       try {
           log.info("getDetailedTatReports() Started..");
           if (fromDate == null || toDate == null) {
               throw new IllegalArgumentException("From Date and To Date are mandatory");
           }

           if (toDate.isBefore(fromDate)) {
               throw new IllegalArgumentException("To Date cannot be before From Date");
           }

           Specification<LabTurnAroundTime> spec = filterTatDetailedReport(
                   investigationId,
                   subChargeCodeId,
                   fromDate,
                   toDate
           );

//           Sort sort = Sort.by("investigation.investigationId");
           List<LabTurnAroundTime> result =
                   labTurnAroundTimeRepository.findAll(spec);

           List<LabDetailedTATReportResponse> response =
                   result.stream()
                           .map(this::mapToDetailResponse)
                           .toList();
           log.info("getDetailedTatReports() Ended..");
           return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
       }catch (Exception e){
           log.error("getDetailedTatReports() error :: ",e);
           return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
       }
    }

    @Override
    public ApiResponse<List<LabSummaryTATReportResponse>> getSummaryTatReports(
            Long investigationId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate) {

        try {
            log.info("getSummaryTatReports() method started..");

            Specification<LabTurnAroundTime> spec =
                    filterTatDetailedReport(investigationId, subChargeCodeId, fromDate, toDate);
            Sort sort = Sort.by("investigation.investigationName");
            List<LabTurnAroundTime> records = labTurnAroundTimeRepository.findAll(spec,sort);

            // Group by Investigation
            Map<DgMasInvestigation, List<LabTurnAroundTime>> grouped =
                    records.stream()
                            .collect(Collectors.groupingBy(
                                    LabTurnAroundTime::getInvestigation,
                                    LinkedHashMap::new,
                                    Collectors.toList()
                            ));

            List<LabSummaryTATReportResponse> responseList = new ArrayList<>();

            for (Map.Entry<DgMasInvestigation, List<LabTurnAroundTime>> entry : grouped.entrySet()) {

                DgMasInvestigation investigation = entry.getKey();
                List<LabTurnAroundTime> list = entry.getValue();

                List<Long> tatHoursList = list.stream()
                        .map(r -> Duration.between(
                                r.getSampleCollectionDateTime(),
                                r.getResultValidationTime()
                        ).toHours())
                        .toList();

                long totalTests = tatHoursList.size();
                int expectedTat = investigation.getTatHours(); // MASTER VALUE

                long breached = tatHoursList.stream()
                        .filter(t -> t > expectedTat)
                        .count();

                long withinTat = totalTests - breached;

                LabSummaryTATReportResponse dto = new LabSummaryTATReportResponse();
                dto.setInvestigationId(investigation.getInvestigationId());
                dto.setInvestigationName(investigation.getInvestigationName());
                dto.setExpectedTatHours(expectedTat);
                dto.setTotalTests((int) totalTests);
                dto.setAverageTatHours((long) tatHoursList.stream().mapToLong(Long::longValue).average().orElse(0));
                dto.setMinTatHours(Collections.min(tatHoursList));
                dto.setMaxTatHours(Collections.max(tatHoursList));
                dto.setNoOfTestsBreached(breached);
                dto.setNoOfTestsWithinTatHour(withinTat);
                dto.setCompliance(
                        totalTests == 0 ? 0 :
                                (int) ((withinTat * 100) / totalTests)
                );

                responseList.add(dto);
            }

            log.info("getSummaryTatReports() method ended..");
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});

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
    public ApiResponse<List<LabAmenedAuditReportResponse>> getAmendAuditReports(
            String phnNum,
            String patientName,
            Long investigationId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        try {
            Specification<LabResultAmendAudit> spec = filterLabAmendAuditReport(
                    phnNum,
                    patientName,
                    investigationId,
                    subChargeCodeId,
                    fromDate,
                    toDate
            );

            List<LabResultAmendAudit> results =
                    amendAuditRepository.findAll(spec);

            return ResponseUtils.createSuccessResponse(results.stream().map(this::mapToLabAmendAuditResponse).toList(), new TypeReference<List<LabAmenedAuditReportResponse>>() {});
        }catch (Exception e){
            log.error("getAmendAuditReports() error :: ",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<OrderTrackingReportResponse>> getOrderTrackingReports(
            String patientName,
            String phnNum,
            LocalDate fromDate,
            LocalDate toDate
    ) {


        try {
            log.info("getOrderTrackingReports() method started...");
//            if ((patientName == null || patientName.isBlank()) &&
//                    (phnNum == null || phnNum.isBlank())) {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Patient Name or Mobile Number is mandatory",HttpStatus.BAD_REQUEST.value());
//            }

//            if (fromDate == null || toDate == null) {
//                return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"From Date and To Date are mandatory",HttpStatus.BAD_REQUEST.value());
//            }

            Specification<DgOrderDt> spec = filterOrderTrackReport(patientName, phnNum, fromDate, toDate);

            Sort sort = Sort.by(Sort.Direction.DESC, "orderhdId.orderDate");

            List<DgOrderDt> entities = dgOrderDtRepository.findAll(spec,sort);

            List<OrderTrackingReportResponse> responses =
                    entities.stream()
                            .map(this::mapToOrderTrackingResponse)
                            .toList();

            log.info("getOrderTrackingReports() method ended...");

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {});


        } catch (Exception e) {
            log.error("getOrderTrackingReports()  error:: ",e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<LabIncompleteInvestigationsReportResponse>> getIncompleteInvestigationReports(Long subChargeCodeId,LocalDate fromDate, LocalDate toDate) {
        try {
            log.info("getIncompleteInvestigationReports() Started...");
            List<Long> orderStatues = List.of(sampleCollectStatusId, sampleRejectStatusId, sampleValidateStatusId, resultEnteredStatusId);
            Specification<DgOrderDt> spec = filterIncompleteInvestigationReports(subChargeCodeId,fromDate, toDate,orderStatues);
            Sort sort= Sort.by(Sort.Direction.DESC,"orderhdId.orderDate");
            List<DgOrderDt> all = dgOrderDtRepository.findAll(spec, sort);
            log.info("getIncompleteInvestigationReports() Ended...");
            return  ResponseUtils.createSuccessResponse(all.stream().map(this::mapToIncompleteReportResponse).toList(), new TypeReference<>() {});
        } catch (Exception e) {
            log.error("getIncompleteInvestigationReports() error ::",e);
            return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<List<SampleRejectionInvestigationReportResponse>> getSampleRejectionReport(Long modalityId, LocalDate fromDate, LocalDate toDate) {

      try {
          log.info("getSampleRejectionReport() Started...");
          Specification<DgSampleCollectionDetails> spec = filterRejectInvestigation(modalityId, fromDate, toDate);
          List<DgSampleCollectionDetails> all = dgSampleCollectionDetailsRepository.findAll(spec);
          log.info("getSampleRejectionReport() Ended...");
          return  ResponseUtils.createSuccessResponse(all.stream().map(this::mapToSampleRejectReport).toList(), new TypeReference<>() {});
      } catch (Exception e) {
        log.error("getSampleRejectionReport() error ::",e);
        return  ResponseUtils.createFailureResponse(null, new TypeReference<>() {},"Internal Server Error",HttpStatus.INTERNAL_SERVER_ERROR.value());
       }
    }


    public static Specification<DgResultEntryDetail> filterLabReports(
            String mobileNo,
            String patientName,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        return (root, query, cb) -> {

            // Avoid duplicate rows due to joins
            query.distinct(true);

            // Joins
            Join<DgResultEntryDetail, DgResultEntryHeader> headerJoin =
                    root.join("resultEntryId", JoinType.INNER);

            Join<DgResultEntryHeader, Patient> patientJoin =
                    headerJoin.join("hinId", JoinType.INNER);

            List<Predicate> predicates = new ArrayList<>();

            predicates.add(
                   cb.equal(root.get("validated"),"y")
            );

            if(fromDate!=null && toDate!=null) {
                predicates.add(
                        cb.between(
                                headerJoin.get("resultDate"),
                                fromDate,
                                toDate
                        )
                );
            }
            /* ------------------ Optional Mobile Number ------------------ */
            if (mobileNo != null && !mobileNo.isBlank()) {
                predicates.add(
                        cb.like(
                                patientJoin.get("patientMobileNumber"),
                                "%" + mobileNo + "%"
                        )
                );
            }

            // Name filter (space-based logic)
            if (patientName != null && !patientName.isBlank()) {

                String[] parts = patientName.trim().split("\\s+");

                if (parts.length == 1) {
                    // First name only
                    predicates.add(
                            cb.like(
                                    cb.lower(patientJoin.get("patientFn")),
                                    "%" + parts[0].toLowerCase() + "%"
                            )
                    );

                } else if (parts.length == 2) {
                    // First + Last
                    predicates.add(
                            cb.and(
                                    cb.like(cb.lower(patientJoin.get("patientFn")), "%" + parts[0].toLowerCase() + "%"),
                                    cb.like(cb.lower(patientJoin.get("patientLn")), "%" + parts[1].toLowerCase() + "%")
                            )
                    );

                } else {
                    // First + Middle + Last
                    predicates.add(
                            cb.and(
                                    cb.like(cb.lower(patientJoin.get("patientFn")), "%" + parts[0].toLowerCase() + "%"),
                                    cb.like(cb.lower(patientJoin.get("patientMn")), "%" + parts[1].toLowerCase() + "%"),
                                    cb.like(cb.lower(patientJoin.get("patientLn")), "%" + parts[2].toLowerCase() + "%")
                            )
                    );
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<LabTurnAroundTime> filterTatDetailedReport(Long investigationId, Long subChargeCodeId, LocalDate fromDate, LocalDate toDate) {

        return (Root<LabTurnAroundTime> root,
                CriteriaQuery<?> query,
                CriteriaBuilder cb) -> {

//            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            /* ---------------- Mandatory: result validated ---------------- */
            predicates.add(
                    cb.isNotNull(root.get("resultValidationTime"))
            );

            /* ---------------- Mandatory: order date range ---------------- */
            Join<LabTurnAroundTime, DgOrderHd> orderJoin =
                    root.join("orderHd", JoinType.INNER);

            predicates.add(
                    cb.between(
                            orderJoin.get("orderDate"),
                            fromDate,
                            toDate
                    )
            );

            /* ---------------- Optional: investigation ---------------- */
            Join<LabTurnAroundTime, DgMasInvestigation> investigationJoin = null;

            if (investigationId != null) {
                investigationJoin =
                        root.join("investigation", JoinType.INNER);

                predicates.add(
                        cb.equal(
                                investigationJoin.get("investigationId"),
                                investigationId
                        )
                );
            }

            /* ---------------- Optional: modality (sub charge code) ---------------- */
            if (subChargeCodeId != null) {

                if (investigationJoin == null) {
                    investigationJoin =
                            root.join("investigation", JoinType.INNER);
                }

                Join<DgMasInvestigation, MasSubChargeCode> subChargeJoin =
                        investigationJoin.join("subChargeCodeId", JoinType.INNER);

                predicates.add(
                        cb.equal(
                                subChargeJoin.get("subId"),
                                subChargeCodeId
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<LabResultAmendAudit> filterLabAmendAuditReport(
            String phnNum,
            String patientName,
            Long investigationId,
            Long subChargeCodeId,
            LocalDate fromDate,
            LocalDate toDate
    ) {

        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
           if(fromDate!=null && toDate!=null) {
               LocalDateTime fromDateTime = fromDate.atStartOfDay();
               LocalDateTime toDateTime = toDate.atTime(23, 59, 59);
               predicates.add(
                       cb.between(
                               root.get("amendedDatetime"),
                               fromDateTime,
                               toDateTime
                       )
               );
           }

            Join<LabResultAmendAudit, Patient> patientJoin = root.join("patient", JoinType.LEFT);

            if (phnNum != null && !phnNum.isBlank()) {
                predicates.add(
                        cb.like(
                                patientJoin.get("patientMobileNumber"),
                                "%" + phnNum.trim() + "%"
                        )
                );
            }

            if (patientName != null && !patientName.isBlank()) {

                String search = "%" + patientName.trim().toLowerCase() + "%";

                Expression<String> firstName = cb.lower(patientJoin.get("patientFn"));
                Expression<String> middleName = cb.lower(patientJoin.get("patientMn"));
                Expression<String> lastName = cb.lower(patientJoin.get("patientLn"));

                // Full name combinations
                Expression<String> fullName1 = cb.lower(
                        cb.concat(
                                cb.concat(firstName, " "),
                                cb.concat(middleName, cb.concat(" ", lastName))
                        )
                );

                Expression<String> fullName2 = cb.lower(
                        cb.concat(
                                cb.concat(firstName, " "),
                                lastName
                        )
                );

                predicates.add(
                        cb.or(
                                cb.like(firstName, search),
                                cb.like(middleName, search),
                                cb.like(lastName, search),
                                cb.like(fullName1, search),
                                cb.like(fullName2, search)
                        )
                );
            }


            Join<LabResultAmendAudit, DgMasInvestigation> invJoin = root.join("investigation", JoinType.LEFT);

            if (investigationId != null) {
                predicates.add(
                        cb.equal(
                                invJoin.get("investigationId"),
                                investigationId
                        )
                );
            }

            if (subChargeCodeId != null) {
                predicates.add(
                        cb.equal(
                                invJoin.get("subChargeCodeId").get("subId"),
                                subChargeCodeId
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    public static Specification<DgOrderDt> filterOrderTrackReport(
            String patientName,
            String mobileNo,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            Join<DgOrderDt, DgOrderHd> orderHdJoin = root.join("orderhdId", JoinType.INNER);
            Join<DgOrderHd, Patient> patientJoin = orderHdJoin.join("patientId", JoinType.INNER);

            if(fromDate!=null && toDate!=null) {
                predicates.add(
                        cb.between(
                                orderHdJoin.get("orderDate"),
                                fromDate,
                                toDate
                        )
                );
            }
            if (mobileNo != null && !mobileNo.isBlank()) {
                predicates.add(
                        cb.like(
                                patientJoin.get("patientMobileNumber"),
                                "%" + mobileNo.trim() + "%"
                        )
                );
            }
            if (patientName != null && !patientName.isBlank()) {

                String[] parts = patientName.trim().split("\\s+");

                if (parts.length == 1) {
                    // First name only
                    predicates.add(
                            cb.like(
                                    cb.lower(patientJoin.get("patientFn")),
                                    "%" + parts[0].toLowerCase() + "%"
                            )
                    );

                } else if (parts.length == 2) {
                    // First + Last
                    predicates.add(
                            cb.and(
                                    cb.like(cb.lower(patientJoin.get("patientFn")), "%" + parts[0].toLowerCase() + "%"),
                                    cb.like(cb.lower(patientJoin.get("patientLn")), "%" + parts[1].toLowerCase() + "%")
                            )
                    );

                } else {
                    // First + Middle + Last
                    predicates.add(
                            cb.and(
                                    cb.like(cb.lower(patientJoin.get("patientFn")), "%" + parts[0].toLowerCase() + "%"),
                                    cb.like(cb.lower(patientJoin.get("patientMn")), "%" + parts[1].toLowerCase() + "%"),
                                    cb.like(cb.lower(patientJoin.get("patientLn")), "%" + parts[2].toLowerCase() + "%")
                            )
                    );
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<DgOrderDt> filterIncompleteInvestigationReports(Long subChargeCodeId,LocalDate fromDate,LocalDate toDate,List<Long> statuses){

        return (root,query,cb)->{
            List<Predicate> predicates= new ArrayList<>();
            Join<DgOrderDt,DgOrderHd> orderHdJoin=root.join("orderhdId",JoinType.INNER);
            Join<DgOrderDt,LabOrderTrackingStatus> statusJoin= root.join("orderTrackingStatus",JoinType.INNER);
            predicates.add(
                    cb.between(orderHdJoin.get("orderDate"),fromDate,toDate)
            );
            if(subChargeCodeId !=null){
                predicates.add(
                        cb.equal(
                                root.get("subChargeid"),subChargeCodeId
                        )
                );
            }
            if(statuses!=null && !statuses.isEmpty()){
                predicates.add(
                        statusJoin.get("orderStatusId").in(statuses)
                );
            }
            return cb.and(predicates.toArray(new Predicate[0]));

        };
    }

    private static Specification<DgSampleCollectionDetails> filterRejectInvestigation(Long subChargeCodeId,LocalDate fromDate,LocalDate toDate){
        return (root,query,cb)->{
            List<Predicate> predicates=new ArrayList<>();
            Join<DgSampleCollectionDetails,DgSampleCollectionHeader> sampleCollectionHeaderJoin= root.join("sampleCollectionHeader",JoinType.INNER);
            Join<DgSampleCollectionHeader,Visit> visitJoin=sampleCollectionHeaderJoin.join("visitId",JoinType.INNER);
            Join<Visit,BillingHeader> billingHeaderJoin=visitJoin.join("billingHd",JoinType.INNER);
            Join<BillingHeader,DgOrderHd> orderHdJoin=billingHeaderJoin.join("hdorder",JoinType.INNER);
            predicates.add(
                    cb.isNotNull(
                            root.get("oldSampleCollectionHdIdForReject")
                    )
            );
            Join<DgSampleCollectionHeader,MasSubChargeCode> subChargeCodeJoin=sampleCollectionHeaderJoin.join("subChargeCode",JoinType.INNER);

            predicates.add(
                    cb.between(orderHdJoin.get("orderDate"),fromDate,toDate)
            );

            if(subChargeCodeId!=null){
                predicates.add(
                        cb.equal(subChargeCodeJoin.get("subId"),subChargeCodeId)
                );
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }


    private AllLabReportResponse mapToResponse(DgResultEntryDetail entity){
        User resultVerifiedBy = userRepo.findById(Long.valueOf(entity.getResultEntryId().getResultVerifiedBy())).orElseThrow(() -> new RuntimeException("User Not Found"));
        AllLabReportResponse response= new AllLabReportResponse();
        Patient patient = entity.getResultEntryId().getHinId();
        response.setOrderHdId((long) entity.getResultEntryId().getOrderHd().getId());
        response.setResultEntryHeaderId(entity.getResultEntryId().getResultEntryId());
        response.setResultEntryDetailsId(entity.getResultEntryDetailId());
        response.setAge(patient.getPatientAge());
        response.setInvestigationDate(entity.getResultEntryId().getOrderHd().getOrderDate());
        response.setGender(patient.getPatientGender().getGenderName());
        response.setRange(entity.getNormalRange());
        response.setResult(entity.getResult());
        response.setResultValidatedBy(resultVerifiedBy.getFullName());
        response.setPhnNum(patient.getPatientMobileNumber());
        response.setPatientName(patient.getPatientMn().trim().isBlank() ? patient.getPatientFn()+" "+patient.getPatientLn() : patient.getPatientFn()+" "+patient.getPatientMn()+" "+patient.getPatientLn());
        response.setUnit(entity.getUomId().getName());
        response.setInvestigationName(entity.getInvestigationId().getInvestigationName());
        response.setResultEnteredBy(entity.getResultEntryId().getResultEnteredBy());
        response.setInRange(isResultWithinRange(entity.getResult(), entity.getNormalRange()));
        return response;
    }


    private LabDetailedTATReportResponse mapToDetailResponse(LabTurnAroundTime entity){
        LabDetailedTATReportResponse response= new LabDetailedTATReportResponse();

        long actualTatHour= Duration.between(entity.getSampleCollectionDateTime(), entity.getResultValidationTime()).toHours();
        int expectedTatHour=entity.getInvestigation().getTatHours();
        long delay=actualTatHour-expectedTatHour;
        response.setTatId(entity.getTurnAroundTimeId());
        response.setOrderId((long) entity.getOrderHd().getId());
        response.setInvestigationName(entity.getInvestigation().getInvestigationName());
        response.setGeneratedSampleId(entity.getGeneratedSampleId()!=null?entity.getGeneratedSampleId():null);
        response.setSampleReceivedDate(entity.getSampleCollectionDateTime());
        response.setReportAuthorizedDate(entity.getResultValidationTime());
        response.setExpectedTatHours(expectedTatHour);
        response.setActualTatHours(actualTatHour);
        response.setDelay(actualTatHour>expectedTatHour?delay:0);
        response.setTatStatus(delay>0?"Breached":"Within");
        response.setTechnicianName(entity.getResultValidatedBy());
        return  response;
    }

    private LabAmenedAuditReportResponse mapToLabAmendAuditResponse(LabResultAmendAudit entity){
        LabAmenedAuditReportResponse response= new LabAmenedAuditReportResponse();
        Patient patient = entity.getPatient();
        response.setAmendId(entity.getAmendmentId());
        response.setSampleId(entity.getGeneratedSampleId());
        response.setInvestigationName(entity.getInvestigation().getInvestigationName());
        response.setPatientName((patient.getPatientMn().trim().isBlank())?patient.getPatientFn()+" "+patient.getPatientLn() : patient.getPatientFn()+" "+patient.getPatientMn()+" "+patient.getPatientLn());
        response.setUnitName(entity.getInvestigation().getUomId().getName());
        response.setOldResult(entity.getOldResult());
        response.setNewResult(entity.getNewResult());
        response.setAuthorizedBy(entity.getAmendedBy());
        response.setReasonForChange(entity.getReasonForChange());
        response.setDateTime(entity.getAmendedDatetime());
        return response;
    }

    private OrderTrackingReportResponse mapToOrderTrackingResponse(DgOrderDt entity){
        OrderTrackingReportResponse response= new OrderTrackingReportResponse();

        if(entity.getOrderTrackingStatus()!=null){
            if(entity.getOrderTrackingStatus().getOrderStatusId()==1){
                response.setOrderStatusName(entity.getOrderTrackingStatus().getOrderStatusName());
                response.setGeneratedSampleId("N/A");
            }else {
                List<DgSampleCollectionDetails> sampleCollectionDetails = dgSampleCollectionDetailsRepository.findByInvestigationId_InvestigationIdAndSampleCollectionHeader_visitId_Id(entity.getInvestigationId().getInvestigationId(), entity.getOrderhdId().getVisitId().getId());
                DgSampleCollectionDetails sampleCollectionDetail =
                        sampleCollectionDetails.stream()
                                .filter(d -> d.getSampleCollDatetime() != null)
                                .max(Comparator.comparing(DgSampleCollectionDetails::getSampleCollDatetime))
                                .orElseThrow();
                response.setOrderStatusName(entity.getOrderTrackingStatus().getOrderStatusName());
                response.setOrderStatusId(entity.getOrderTrackingStatus().getOrderStatusId());
                response.setGeneratedSampleId(sampleCollectionDetail.getSampleGeneratedId());
            }
        }
        Patient patient = entity.getOrderhdId().getPatientId();
        response.setDgOrderHdId((long) entity.getOrderhdId().getId());
        response.setOrderNum(entity.getOrderhdId().getOrderNo());
        response.setAge(patient.getPatientAge());
        response.setGender(patient.getPatientGender().getGenderName());
        response.setMobileNum(patient.getPatientMobileNumber());
        response.setPatientName(patient.getPatientMn().trim().isBlank()?patient.getPatientFn()+" "+patient.getPatientLn():patient.getFullName());
        response.setInvestigationName(entity.getInvestigationId().getInvestigationName());
        response.setOrderDate(entity.getOrderhdId().getOrderDate());

        return  response;
    }

    private LabIncompleteInvestigationsReportResponse mapToIncompleteReportResponse(DgOrderDt entity){
        LabIncompleteInvestigationsReportResponse response= new LabIncompleteInvestigationsReportResponse();
        Patient patient = entity.getOrderhdId().getPatientId();
        List<DgSampleCollectionDetails> sampleCollectionDetails = dgSampleCollectionDetailsRepository.findByInvestigationId_InvestigationIdAndSampleCollectionHeader_visitId_Id(entity.getInvestigationId().getInvestigationId(), entity.getOrderhdId().getVisitId().getId());

        DgSampleCollectionDetails sampleCollectionDetail =
                sampleCollectionDetails.stream()
                        .filter(d -> d.getSampleCollDatetime() != null)
                        .max(Comparator.comparing(DgSampleCollectionDetails::getSampleCollDatetime))
                        .orElseThrow();
        response.setSampleId(sampleCollectionDetail.getSampleGeneratedId());
        response.setOrderNo(entity.getOrderhdId().getOrderNo());
        response.setOrderDate(entity.getOrderhdId().getOrderDate());
        response.setAge(patient.getPatientAge());
        response.setGender(patient.getPatientGender().getGenderName());
        response.setMobileNum(patient.getPatientMobileNumber());
        response.setPatientName(patient.getPatientMn().trim().isBlank()?patient.getPatientFn()+" "+patient.getPatientLn():patient.getFullName());
        response.setInvestigationName(entity.getInvestigationId().getInvestigationName());
        if(entity.getOrderTrackingStatus()!=null){
            response.setCurrentStatus(entity.getOrderTrackingStatus().getOrderStatusName());
        }
        return  response;
    }

    private SampleRejectionInvestigationReportResponse mapToSampleRejectReport(DgSampleCollectionDetails entity){
        SampleRejectionInvestigationReportResponse response=new SampleRejectionInvestigationReportResponse();
        DgOrderHd dgOrderHd = entity.getSampleCollectionHeader().getVisitId().getBillingHd().getHdorder();
//        DgOrderDt dgOrderDt = dgOrderDtRepository.findByOrderhdId_IdAndInvestigationId_InvestigationId(dgOrderHd.getId(), entity.getInvestigationId().getInvestigationId());
        Patient patient = entity.getSampleCollectionHeader().getPatientId();
        response.setOrderNo(dgOrderHd.getOrderNo());
        response.setOrderDate(dgOrderHd.getOrderDate());
        response.setPatientName(patient.getFullName());
        response.setAge(patient.getPatientAge());
        response.setGender(patient.getPatientGender().getGenderName());
        response.setMobileNum(patient.getPatientMobileNumber());
        response.setInvestigationName(entity.getInvestigationId().getInvestigationName());
        response.setSampleId(entity.getSampleGeneratedId());
//        response.setOrderStatus(dgOrderDt.getOrderTrackingStatus()!=null ?dgOrderDt.getOrderTrackingStatus().getOrderStatusName():null);
        response.setOrderStatus("Rejected");
        response.setRejectionReason(entity.getRejected_reason());
        response.setModalityName(entity.getSampleCollectionHeader().getSubChargeCode().getSubName());
        return  response;
    }


    private Boolean isResultWithinRange(String resultStr, String normalRangeStr) {
        if (resultStr == null || normalRangeStr == null || resultStr.trim().isBlank() || normalRangeStr.trim().isBlank()) {
            return null;
        }
        resultStr = resultStr.trim();
        normalRangeStr = normalRangeStr.trim();

        if (normalRangeStr.matches("[-+]?\\d*\\.?\\d+\\s*-\\s*[-+]?\\d*\\.?\\d+")) {
            return numericRangeCheck(resultStr, normalRangeStr);
        }

        if (normalRangeStr.matches("(<=|>=|<|>)\\s*[-+]?\\d*\\.?\\d+")) {
            return inequalityCheck(resultStr, normalRangeStr);
        }
        return qualitativeCheck(resultStr, normalRangeStr);
    }


    private Boolean numericRangeCheck(String resultStr, String range) {
        try {
            String[] parts = range.split("-");
            double min = Double.parseDouble(parts[0].trim());
            double max = Double.parseDouble(parts[1].trim());
            double result = Double.parseDouble(resultStr);

            return result >= min && result <= max;
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean inequalityCheck(String resultStr, String range) {
        try {
            double result = Double.parseDouble(resultStr);
            String operator = range.replaceAll("[0-9.\\s]", "");
            double limit = Double.parseDouble(range.replaceAll("[^0-9.]", ""));

            return switch (operator) {
                case "<"  -> result < limit;
                case "<=" -> result <= limit;
                case ">"  -> result > limit;
                case ">=" -> result >= limit;
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private Boolean qualitativeCheck(String result, String normal) {
        result = result.toLowerCase();
        normal = normal.toLowerCase();
        if (normal.equals("negative")) {
            return result.equals("negative") || result.equals("nil");
        }
        if (normal.equals("positive")) {
            return result.equals("positive") || result.contains("+");
        }
        return result.equals(normal);
    }
}
