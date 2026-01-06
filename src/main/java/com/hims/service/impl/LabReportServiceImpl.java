package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.DgResultEntryDetailRepository;
import com.hims.entity.repository.LabTurnAroundTimeRepository;
import com.hims.entity.repository.UserRepo;
import com.hims.response.AllLabReportResponse;
import com.hims.response.ApiResponse;
import com.hims.response.LabDetailedTATReportResponse;
import com.hims.response.LabSummaryTATReportResponse;
import com.hims.service.LabReportService;
import com.hims.utils.ResponseUtils;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LabReportServiceImpl implements LabReportService {

    private final DgResultEntryDetailRepository resultEntryDetailRepository;

    private final UserRepo userRepo;

    private final LabTurnAroundTimeRepository labTurnAroundTimeRepository;


    @Override
    public ApiResponse<List<AllLabReportResponse>> getAllLabReports(String phnNum, String patientName, LocalDate fromDate,LocalDate toDate) {
        try {
            log.info("getAllLabReports() Started..." );
            if (fromDate == null || toDate == null) {
                throw new IllegalArgumentException("From Date and To Date are mandatory");
            }
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

            List<LabTurnAroundTime> records = labTurnAroundTimeRepository.findAll(spec);

            // Group by Investigation
            Map<DgMasInvestigation, List<LabTurnAroundTime>> grouped =
                    records.stream()
                            .filter(r -> r.getResultValidationTime() != null
                                    && r.getSampleCollectionDateTime() != null)
                            .collect(Collectors.groupingBy(LabTurnAroundTime::getInvestigation));

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

            /* ------------------ Mandatory Date Filter ------------------ */
            predicates.add(
                    cb.between(
                            headerJoin.get("resultDate"),
                            fromDate,
                            toDate
                    )
            );

            /* ------------------ Optional Mobile Number ------------------ */
            if (mobileNo != null && !mobileNo.isBlank()) {
                predicates.add(
                        cb.like(
                                patientJoin.get("patientMobileNumber"),
                                "%" + mobileNo + "%"
                        )
                );
            }

            /* ------------------ Optional Patient Name ------------------ */
            if (patientName != null && !patientName.isBlank()) {

                Expression<String> fullNameExpr = cb.concat(
                        cb.concat(
                                cb.coalesce(patientJoin.get("patientFn"), ""),
                                " "
                        ),
                        cb.concat(
                                cb.coalesce(patientJoin.get("patientMn"), ""),
                                cb.concat(
                                        " ",
                                        cb.coalesce(patientJoin.get("patientLn"), "")
                                )
                        )
                );

                predicates.add(
                        cb.like(
                                cb.lower(fullNameExpr),
                                "%" + patientName.toLowerCase() + "%"
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<LabTurnAroundTime> filterTatDetailedReport(Long investigationId, Long subChargeCodeId, LocalDate fromDate, LocalDate toDate) {

        return (Root<LabTurnAroundTime> root,
                CriteriaQuery<?> query,
                CriteriaBuilder cb) -> {

            query.distinct(true);

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
        return response;
    }


    private LabDetailedTATReportResponse mapToDetailResponse(LabTurnAroundTime entity){
        LabDetailedTATReportResponse response= new LabDetailedTATReportResponse();

        long actualTatHour= Duration.between(entity.getSampleCollectionDateTime(), entity.getResultValidationTime()).toHours();;
        int expectedTatHour=entity.getInvestigation().getTatHours();
        long delay=actualTatHour-expectedTatHour;
        response.setTatId(entity.getTurnAroundTimeId());
        response.setOrderId((long) entity.getOrderHd().getId());
        response.setInvestigationName(entity.getInvestigation().getInvestigationName());
        response.setGeneratedSampleId(entity.getGeneratedSampleId()!=null?entity.getGeneratedSampleId():null);
        response.setSampleReceivedDate(entity.getSampleCollectionDateTime());
        response.setReportAuthorizedDate(entity.getSampleValidatedDateTime());
        response.setExpectedTatHours(expectedTatHour);
        response.setActualTatHours(actualTatHour);
        response.setDelay(actualTatHour>expectedTatHour?delay:0);
        response.setTatStatus(delay>0?"Breached":"Within");
        response.setTechnicianName(entity.getResultValidatedBy());
        return  response;
    }

}
