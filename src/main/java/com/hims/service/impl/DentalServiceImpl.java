package com.hims.service.impl;

import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.DentalDetailsRequest;
import com.hims.response.ApiResponse;
import com.hims.service.DentalService;
import com.hims.request.DentalExaminationRequest;
import com.hims.request.DentalProcedureRequest;
//import com.hims.request.DentalRequest;
//import com.hims.request.DentalToothConditionRequest;
import com.hims.request.DentalToothRequest;
import com.hims.service.DentalService;
import com.hims.utils.AuthUtil;
import com.hims.utils.ResponseUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DentalServiceImpl implements DentalService {

    private final OpdPatientDentalSummaryRepository dentalSummaryRepository;
    private final OpdToothPatientConditionRepository toothConditionRepository;
    private final DentalProcedureToothRepository dentalProcedureToothRepository;
    private final MasToothMasterRepository  masToothMasterRepository;
    private final MasToothConditionRepository  masToothConditionRepository;

    private final AuthUtil authUtil;

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
    public ApiResponse<String> createOrUpdateDentalDetails(
            DentalDetailsRequest request,
            Patient patient,
            Visit visit,
            User user,
            Long departmentId) {

        if (request == null || request.getDentalExamination() == null) {
            return ResponseUtils.createSuccessResponse(
                    "No dental examination details found",
                    null
            );
        }

        DentalExaminationRequest examination = request.getDentalExamination();

        Long patientId = patient.getId();
        Long visitId = visit.getId();

        log.info(
                "Saving dental details for patientId={}, visitId={}",
                patientId,
                visitId
        );

        // ============================================================
        // 1. SAVE / UPDATE DENTAL SUMMARY
        // ============================================================

        OpdPatientDentalSummary summary = dentalSummaryRepository.findByPatientIdAndVisitId(patientId, visitId)
                        .orElseGet(OpdPatientDentalSummary::new);

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
        summary.setLastUpdatedBy(user.getFullName());
        summary.setLastUpdateDate(LocalDateTime.now());

        OpdPatientDentalSummary savedSummary = dentalSummaryRepository.save(summary);
        log.info("Dental summary saved successfully. ID={}", savedSummary.getSummaryId());
        toothConditionRepository.deleteByPatientIdAndVisitId(patientId, visitId);

        List<DentalExaminationRequest.DentalToothConditionRequest> toothConditions = examination.getToothConditions();
        if (toothConditions != null && !toothConditions.isEmpty()) {
            List<OpdToothPatientCondition> entities = toothConditions.stream()
                            .map(tooth -> {

                                OpdToothPatientCondition entity = new OpdToothPatientCondition();

                                entity.setPatientId(patientId);
                                entity.setVisitId(visitId);
                                entity.setTooth(masToothMasterRepository.getReferenceById(tooth.getToothId()));
                                entity.setCondition(masToothConditionRepository.getReferenceById(tooth.getConditionId()));
                                entity.setStatus(AppConstants.STATUS_N);
                                entity.setLastUpdatedBy(user.getFullName());
                                entity.setLastUpdateDate(LocalDateTime.now());
                                entity.setStatus(AppConstants.STATUS_N);
                                entity.setCreatedBy(user.getFullName());
                                entity.setCreatedDate(LocalDateTime.now());
                                return entity;
                            })
                            .toList();

            toothConditionRepository.saveAll(entities);
            log.info( "Saved {} tooth conditions for patientId={}, visitId={}", entities.size(), patientId, visitId);
        }
        log.info("Dental details saved successfully for patientId={}, visitId={}", patientId, visitId);
        return ResponseUtils.createSuccessResponse("Dental details saved successfully", null);
    }
}

