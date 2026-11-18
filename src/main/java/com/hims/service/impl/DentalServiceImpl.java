package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.OpdPatientDentalSummary;
import com.hims.entity.OpdToothPatientCondition;
import com.hims.entity.Patient;
import com.hims.entity.Visit;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.request.DentalDetailsRequest;
import com.hims.request.DentalExaminationRequest;
import com.hims.response.*;
import com.hims.service.DentalService;
import com.hims.service.UserContextService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DentalServiceImpl implements DentalService {

    private final OpdPatientDentalSummaryRepository dentalSummaryRepository;
    private final OpdToothPatientConditionRepository toothConditionRepository;
    private final DentalProcedureToothRepository dentalProcedureToothRepository;
    private final MasToothMasterRepository masToothMasterRepository;
    private final MasToothConditionRepository masToothConditionRepository;

    private final AuthUtil authUtil;

    private final ProcedureSessionRepository procedureSessionRepository;

    @Autowired
    private UserContextService userContextService;

//    @Override
//    @Transactional
//    public void saveDentalDetails(DentalRequest request) {
//
//        OpdPatientDentalSummary summary = saveDentalSummary(request);
//
//        saveToothConditions(request);
//
//        saveProcedureTeeth(request);
//    }
//
//    private OpdPatientDentalSummary saveDentalSummary(DentalRequest request) {
//        DentalExaminationRequest examination = request.getExamination();
//        OpdPatientDentalSummary summary = OpdPatientDentalSummary.builder()
//                .patientId(request.getPatientId())
//                .visitId(request.getVisitId())
//                .totalTeeth(examination.getTotalTeeth())
//                .missingTeeth(examination.getMissingTeeth())
//                .unsalvageableTeeth(examination.getUnsalvageableTeeth())
//                .affectedTeeth(examination.getAffectedTeeth())
//                .dentalDiseaseScore(examination.getDentalDiseaseScore())
//                .notes(examination.getNotes())
//                .ongoingProcedures(examination.getOngoingProcedures())
//                .status(AppConstants.STATUS_N)
//                .createdBy(authUtil.getUserName())
//                .lastUpdatedBy(authUtil.getUserName())
//                .build();
//
//        return dentalSummaryRepository.save(summary);
//    }
//
//    private void saveToothConditions(DentalRequest request) {
//
//        if (request.getToothConditions() == null || request.getToothConditions().isEmpty()) {
//            return;
//        }
//
//        for (DentalToothConditionRequest conditionRequest : request.getToothConditions()) {
//
//            OpdToothPatientCondition condition =
//                    OpdToothPatientCondition.builder()
//                            .patientId(request.getPatientId())
//                            .visitId(request.getVisitId())
//                            .toothId(conditionRequest.getToothId())
//                            .conditionId(conditionRequest.getConditionId())
//                            .remarks(conditionRequest.getRemarks())
//                            .status("Y")
//                            .createdBy(authUtil.getUserName())
//                            .lastUpdatedBy(authUtil.getUserName())
//                            .build();
//
//            toothConditionRepository.save(condition);
//        }
//    }
//
//    private void saveProcedureTeeth(DentalRequest request) {
//
//        if (request.getProcedures() == null || request.getProcedures().isEmpty()) {
//            return;
//        }
//
//        for (DentalProcedureRequest procedure : request.getProcedures()) {
//
//            /*
//             * IMPORTANT:
//             *
//             * You need the actual procedureDtId here.
//             *
//             * This should be returned by your existing
//             * ProcedureService after procedure_dt is created.
//             */
//            Integer procedureDtId = getProcedureDtId(procedure);
//
//            if (procedure.getDentalProcedureTeeth() == null || procedure.getDentalProcedureTeeth().isEmpty()) {
//                continue;
//            }
//
//            for (DentalToothRequest tooth : procedure.getDentalProcedureTeeth()) {
//
//                DentalProcedureTooth entity =
//                        DentalProcedureTooth.builder()
//                                .procedureDtId(procedureDtId)
//                                .toothId(tooth.getToothId())
//                                .toothSurface(tooth.getToothSurface())
//                                .remarks(procedure.getRemarks())
//                                .status("Y")
//                                .createdBy(authUtil.getUserName())
//                                .lastUpdatedBy(authUtil.getUserName())
//                                .build();
//
//                dentalProcedureToothRepository.save(entity);
//            }
//        }
//    }
//
//    private Integer getProcedureDtId(DentalProcedureRequest procedure) {
//
//        return procedure.getProcedureId();
//    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public ApiResponse<String> createOrUpdateDentalDetails(DentalDetailsRequest request, Patient patient, Visit visit, Long departmentId) {

        UserContext userContext = userContextService.getCurrentUserContext();

        if (request == null || request.getDentalExamination() == null) {
            return ResponseUtils.createSuccessResponse("No dental examination details found", null);
        }

        DentalExaminationRequest examination = request.getDentalExamination();

        Long patientId = patient.getId();
        Long visitId = visit.getId();

        log.info("Saving dental details for patientId={}, visitId={}", patientId, visitId);
        OpdPatientDentalSummary summary = dentalSummaryRepository.findByPatientIdAndVisitId(patientId, visitId).orElseGet(OpdPatientDentalSummary::new);

        summary.setPatientId(patientId);
        summary.setVisitId(visitId);
        summary.setTotalTeeth(examination.getTotalTeeth());
        summary.setMissingTeeth(examination.getMissingTeeth());
        summary.setUnsalvageableTeeth(examination.getUnsalvageableTeeth());
        summary.setAffectedTeeth(examination.getAffectedTeeth());
        summary.setDentalDiseaseScore(examination.getDentalDiseaseScore());
        summary.setOngoingProcedures(examination.getOngoingProcedures());
        summary.setNotes(examination.getNotes());

        summary.setStatus(AppConstants.STATUS_N);
        summary.setLastUpdatedBy(userContext.getUserFullName());
        summary.setLastUpdateDate(LocalDateTime.now());

        OpdPatientDentalSummary savedSummary = dentalSummaryRepository.save(summary);
        log.info("Dental summary saved successfully. ID={}", savedSummary.getSummaryId());
        toothConditionRepository.deleteByPatientIdAndVisitId(patientId, visitId);

        List<DentalExaminationRequest.DentalToothConditionRequest> toothConditions = examination.getToothConditions();
        if (toothConditions != null && !toothConditions.isEmpty()) {
            List<OpdToothPatientCondition> entities = toothConditions.stream().map(tooth -> {

                OpdToothPatientCondition entity = new OpdToothPatientCondition();

                entity.setPatientId(patientId);
                entity.setVisitId(visitId);
                entity.setTooth(masToothMasterRepository.getReferenceById(tooth.getToothId()));
                entity.setCondition(masToothConditionRepository.getReferenceById(tooth.getConditionId()));
                entity.setStatus(AppConstants.STATUS_N);
                entity.setLastUpdatedBy(userContext.getUserFullName());
                entity.setLastUpdateDate(LocalDateTime.now());
                entity.setStatus(AppConstants.STATUS_N);
                entity.setCreatedBy(userContext.getUserFullName());
                entity.setCreatedDate(LocalDateTime.now());
                return entity;
            }).toList();

            toothConditionRepository.saveAll(entities);
            log.info("Saved {} tooth conditions for patientId={}, visitId={}", entities.size(), patientId, visitId);
        }
        log.info("Dental details saved successfully for patientId={}, visitId={}", patientId, visitId);
        return ResponseUtils.createSuccessResponse("Dental details saved successfully", null);
    }

    @Override
    public ApiResponse<Page<DentalSummaryResponse>> getPreviousDentalDetails(Long patientId,int page,int size) {
        try {
            log.info("getPreviousDentalDetails method started for patientId={}", patientId);
            Pageable pageable= PageRequest.of(page, size);
            Page<DentalSummaryResponse> response=dentalSummaryRepository.findByPatientId(patientId,pageable);
            log.info("getPreviousDentalDetails method ended for patientId={}", patientId);

            return  ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
        }catch (SDDException e) {
            log.error("Error getPreviousDentalDetails for patientId={}", patientId, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {},
                    e.getMessage(),
                    HttpStatus.BAD_REQUEST.value());
        }catch (Exception e) {
            log.error("Error getPreviousDentalDetails for patientId={}", patientId, e);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {
                    },
                    "An unexpected error occurred while retrieving previous dental details.",
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

    }

    public ApiResponse<Page<DentalProcedureHistoryResponse>> getDentalProcedureHistory(Long patientId, int page, int size) {

        try {
        Pageable pageable = PageRequest.of(page, size);

        Page<Long> idPage = dentalProcedureToothRepository.findProcedureDtIdsByPatient(patientId, pageable);

        if (idPage.isEmpty()) {
            return ResponseUtils.createSuccessResponse(
                    new PageImpl<>(Collections.emptyList(), pageable, idPage.getTotalElements()),
                    new TypeReference<>() {});
        }

        List<DentalProcedureHistoryRow> rows =
                dentalProcedureToothRepository.findDentalProcedureHistoryRowsByDtIds(idPage.getContent());


        Map<Long, DentalProcedureHistoryResponse> resultMap = new HashMap<>();
        Map<Long, Set<String>> teethMap = new HashMap<>();

        for (DentalProcedureHistoryRow row : rows) {
            DentalProcedureHistoryResponse resp = resultMap.get(row.getProcedureDtId());
            if (resp == null) {
                resp = new DentalProcedureHistoryResponse();
                resp.setProcedureDtId(row.getProcedureDtId());
                resp.setProcedureHdId(row.getProcedureHdId());
                resp.setProcedureName(row.getProcedureName());
                resp.setAdvisedBy(row.getAdvisedBy());
                resp.setAdvisedDate(row.getAdvisedDate());
                resp.setPlannedSittings(row.getPlannedSittings());
                resp.setCompletedSittings(row.getCompletedSittings());
                resp.setStatus(row.getStatus());
                resp.setFirstSittingDate(row.getFirstSittingDate());
                resultMap.put(row.getProcedureDtId(), resp);
                teethMap.put(row.getProcedureDtId(), new LinkedHashSet<>());
            }
            if (row.getTeethNumber() != null) {
                teethMap.get(row.getProcedureDtId()).add(row.getTeethNumber());
            }
        }

        List<DentalProcedureHistoryResponse> content = new ArrayList<>();
        for (Long dtId : idPage.getContent()) {
            DentalProcedureHistoryResponse resp = resultMap.get(dtId);
            if (resp != null) {
                resp.setTeethNumber(String.join(",", teethMap.get(dtId)));
                content.add(resp);
            }
        }

        return ResponseUtils.createSuccessResponse(
                new PageImpl<>(content, pageable, idPage.getTotalElements()),
                new TypeReference<>() {});
      }catch (Exception e) {
        log.error("Error getPreviousDentalDetails for patientId={}", patientId, e);
        return ResponseUtils.createFailureResponse(
                null,
                new TypeReference<>() {
                },
                "An unexpected error occurred while retrieving previous dental details.",
                HttpStatus.INTERNAL_SERVER_ERROR.value());
     }
    }

    @Override
    @Transactional
    public ApiResponse<List<ProcedureSessionHistoryResponse>> getProcedureSessionHistory(
            Long procedureDtId) {

        return ResponseUtils.createSuccessResponse(
                procedureSessionRepository.findSessionHistoryByProcedureDtId(procedureDtId),
                new TypeReference<>() {}
        );
    }


}

