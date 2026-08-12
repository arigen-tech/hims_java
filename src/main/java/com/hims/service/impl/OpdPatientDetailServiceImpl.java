package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.projection.PrescriptionDetailProjection;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.helperUtil.HelperUtils;
import com.hims.mapper.TreatmentData;
import com.hims.projection.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.*;
import com.hims.utils.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.hims.helperUtil.ConverterUtils.ageCalculator;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpdPatientDetailServiceImpl implements OpdPatientDetailService {


    private final OpdPatientDetailRepository opdPatientDetailRepository;

    private final DischargeIcdCodeRepository dischargeIcdCodeRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final MasDepartmentRepository departmentRepository;
    private final MasHospitalRepository hospitalRepository;
    private final UserRepo userRepository;

    private final MasIcdRepository masIcdRepository;
    private final StockFound stockFound;

    private final DgMasInvestigationRepository dgMasInvestigationRepository;
    private final LabHdRepository dgOrderHdRepo;

    private final MasInvestigationPriceDetailsRepository masInvestigationPriceDetailsRepository;

    private final LabDtRepository dgOrderDtRepo;

    private final PatientPrescriptionHdRepository patientPrescriptionHdRepository;

    private final PatientPrescriptionDtRepository patientPrescriptionDtRepository;

    private final AuthUtil authUtil;

    private final MasStoreItemRepository masStoreItemRepository;

    private final MasCareLevelRepo masCareLevelRepository;

    private final MasWardCategoryRepository masWardCategoryRepository;

    private final MasDepartmentRepository masDepartmentRepository;

    private final LabOrderTrackingStatusRepository labOrderTrackingStatusRepository;

    private final RadOrderHdRepository radOrderHdRepository;

    private final RadOrderDtRepository radOrderDtRepository;

    private final MasServiceCategoryRepository masServiceCategoryRepository;

    private final PatientPrescriptionDtRepository prescriptionDtRepository;

    private final OpdOpthDetailsService opdOpthDetailsService;
    private final OpdObgDetailsService opdObgDetailsService;
    private final OpdEntDetailsService opdEntDetailsService;
    private final OpdPatientPregnancyDetailsRepository opdPatientPregnancyDetailsRepository;
    private final OpdPsychiatryAssessmentDetailRepository opdPsychiatryAssessmentDetailRepository;
    private final OpdPsychiatryAssessmentHeaderRepository opdPsychiatryAssessmentHeaderRepository;
    private final MasQuestionHeadingRepository masQuestionHeadingRepository;
    private final OpdQuestionMasterRepository opdQuestionMasterRepository;
    private final MasQuestionOptionValueRepository masQuestionOptionValueRepository;
    private final TransactionSequenceService transactionSequenceService;
    private final MasFrequencyRepository masFrequencyRepository;

    @Value("${hos.define.storeDay}")
    private Integer hospDefinedDays;

    @Value("${hos.define.storeId}")
    private Integer deptIdStore;

    @Value("${lab.track-order-status-reg.ordered}")
    private Long orderedStatusId;

    @Value("${prescription.history.days}")
    private Integer prescriptionHistoryDays;

    @Value("${app.radiologyDepartment}")
    private Integer radiologyDepartment;

    @Value("${app.laboratoryDepartment}")
    private Integer laboratoryDepartment;


    @Autowired
    HelperUtils helperUtils;



    @Override
    public ApiResponse<OpdPatientVitalResponse> getOpdPatientByVisit(Long visitId) {
        if (visitId == null) {
            throw new IllegalArgumentException("Visit ID must not be null");
        }
        OpdPatientDetail opdPObj = opdPatientDetailRepository.findByVisitId(visitId);
        if (opdPObj == null) {
            return ResponseUtils.createNotFoundResponse("OPD details not found for visitId: " + visitId, 404);
        }
        OpdPatientVitalResponse responseDto = mapToVitalResponse(opdPObj);
        return ResponseUtils.createSuccessResponse(responseDto, new TypeReference<>() {
        });
    }

    private OpdPatientVitalResponse mapToVitalResponse(OpdPatientDetail opd) {
        OpdPatientVitalResponse res = new OpdPatientVitalResponse();
        res.setOpdPatientDetailsId(opd.getOpdPatientDetailsId());
        res.setHeight(opd.getHeight());
        res.setWeight(opd.getWeight());
        res.setPulse(opd.getPulse());
        res.setTemperature(opd.getTemperature());
        res.setRr(opd.getRr());
        res.setBmi(opd.getBmi());
        res.setSpo2(opd.getSpo2());
        res.setBpSystolic(opd.getBpSystolic());
        res.setBpDiastolic(opd.getBpDiastolic());
        res.setMlcFlag(opd.getMlcFlag());

        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<OpdPatientDetailResponseDTO> createOpdPatientDetail(OpdPatientDetailCreateRequest request) {
        validateCreateRequest(request);
        User user = getCurrentUser();
        if (user == null || user.getHospital() == null) {
            throw new SDDException("user", 401, "Authenticated user or hospital not found");
        }
        Patient patient = getPatient(request.getPatientId());
        Visit visit = getVisit(request.getVisitId());
        Long deptId = authUtil.getCurrentDepartmentId();
        OpdPatientDetail opd = opdPatientDetailRepository.findByVisit_Id(request.getVisitId()).orElseGet(() -> {
                    log.info("Creating new OPD Patient Detail for visit ID: {}", request.getVisitId());
                    return new OpdPatientDetail();
                });

        if ((request.getWorkingDiagnosis() == null || request.getWorkingDiagnosis().isBlank()) && (request.getIcdDiagnosis() == null || request.getIcdDiagnosis().isEmpty())) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.MANDATORY_DIAGNOSIS_MESSAGE, 400);
        }
        mapBasicVitalDetails(opd, request);
        mapClinicalDetails(opd, request);
        mapGeneralDetails(opd, patient, visit, user, deptId);
        handleAdmission(opd, request);
        handleFollowUp(opd, request);
        handleReferral(opd, request);
        opd.setLabFlag(AppConstants.STATUS_N.toLowerCase());
        opd.setRadioFlag(AppConstants.STATUS_N.toLowerCase());
        OpdPatientDetail saved = opdPatientDetailRepository.save(opd);
        log.info("Saved OPD detail with ID: {}", saved.getOpdPatientDetailsId());

        //saving diagnosis data for opd - Start
        List<Long> diagIdList = Optional.ofNullable(request.getIcdDiagnosis())
                .orElse(Collections.emptyList())
                .stream()
                .map(OpdPatientDetailCreateRequest.IcdDiagnosis::getIcdId)
                .collect(Collectors.toList());
        saveOrUpdateIcdDiagnosis(diagIdList, saved.getOpdPatientDetailsId(), request.getVisitId(), user.getUserId());
        //diagnosis - End

        boolean hasLabInvestigations = false;
        boolean hasRadioInvestigations = false;
        if (request.getInvestigation() != null && !request.getInvestigation().isEmpty()) {
            if (request.getInvestigation().stream().anyMatch(i -> i == null || i.getInvestigationDate() == null)) {
                throw new SDDException("investigation", 400, "Investigation date cannot be null");
            }
            LabOrderTrackingStatus labOrderedStatus = labOrderTrackingStatusRepository.findById(orderedStatusId)
                    .orElseThrow(() -> new SDDException("status", 500, "Ordered status not found with id: " + orderedStatusId));

            // Group investigations by department
            Map<Long, Map<LocalDate, List<OpdPatientDetailCreateRequest.Investigation>>> grouped = request.getInvestigation().stream().filter(Objects::nonNull).collect(Collectors.groupingBy(inv -> helperUtils.getDepartmentFromInvestigation(inv.getId()), Collectors.groupingBy(OpdPatientDetailCreateRequest.Investigation::getInvestigationDate)));

            if (grouped.containsKey(Long.valueOf(laboratoryDepartment))) {
                log.info("Processing LAB investigations");
                processLabInvestigations(grouped.get(Long.valueOf(laboratoryDepartment)), patient, visit, user, deptId,
                        transactionSequenceService.generateTransactionNumber(HMISTransaction.LAB_NO, user.getHospital().getId()), labOrderedStatus);
                hasLabInvestigations = true;
            }
            if (grouped.containsKey(Long.valueOf(radiologyDepartment))) {
                log.info("Processing RADIOLOGY investigations");
                processRadiologyInvestigations(grouped.get(Long.valueOf(radiologyDepartment)), patient, visit, user);
                hasRadioInvestigations = true;
            }
        }
        if (hasLabInvestigations) {
            saved.setLabFlag(AppConstants.STATUS_Y.toLowerCase());
        }
        if (hasRadioInvestigations) {
            saved.setRadioFlag(AppConstants.STATUS_Y.toLowerCase());
        }

        if (request.getTreatment() != null && !request.getTreatment().isEmpty()) {
            List<TreatmentData> treatments = request.getTreatment()
                    .stream()
                    .map(t -> new TreatmentData(
                            null,
                            t.getItemId(),
                            t.getDosage(),
                            t.getFrequency(),
                            t.getDays(),
                            t.getTotal(),
                            t.getInstraction()
                    ))
                    .toList();
            saveOrUpdateTreatments(treatments,patient,visit,user,deptId);
        }

        if (request.getOphthalmologyExaminationDetails() != null) {
            OpdOpthDetailsRequest opthRequest = request.getOphthalmologyExaminationDetails();
            opthRequest.setPatientId(patient.getId());
            opthRequest.setVisitId(visit.getId());
            ApiResponse<String> response = opdOpthDetailsService.opdVisionExaminationDetailsSaveOrUpdate(opthRequest);
            if (response == null || response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("ophthalmology", 500, response != null ? response.getMessage() : "Failed to save ophthalmology details");
            }
        }
        if (request.getOpdObgDetailsRequest() != null) {
            request.getOpdObgDetailsRequest().setPatientId(patient.getId());
            request.getOpdObgDetailsRequest().setVisitId(visit.getId());
            ApiResponse<String> response = opdObgDetailsService.createOrUpdateObgDetails(request.getVisitId(), request.getOpdObgDetailsRequest());
            if (response == null || response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("obg", 500, response != null ? response.getMessage() : "Failed to save OBG details");
            }
        }
        if (request.getEntExaminationDetails() != null) {
            ApiResponse<String> response = opdEntDetailsService.createOrUpdateEntDetails(request.getVisitId(), request.getEntExaminationDetails());
            if (response == null || response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("ent", 500, response != null ? response.getMessage() : "Failed to save ENT details");
            }
        }
        if (request.getPregnancyDetails() != null) {
            handlePregnancyDetails(saved, request.getPregnancyDetails(), user);
        }
        if (request.getPsychiatricDetailsRequests() != null && !request.getPsychiatricDetailsRequests().isEmpty()) {
            log.info("Saving Psychiatric Assessment Header and Details for OPD ID: {}", saved.getOpdPatientDetailsId());
            saveOrUpdatePsychiatricAssessment(request.getPsychiatricDetailsRequests(),request.getTopicId(), visit, saved, authUtil.getCurrentUser());
        }
        opdPatientDetailRepository.save(saved);
        closeVisit(visit);
        log.info("Successfully completed OPD patient detail creation for visit ID: {}", visit.getId());
        return ResponseUtils.createSuccessResponse(null, new TypeReference<>() {
        });
    }


    private void validateCreateRequest(OpdPatientDetailCreateRequest request) {
        if (request == null) {
            throw new SDDException("request", 400, "Request cannot be null");
        }
        if (request.getPatientId() == null) {
            throw new SDDException("patient", 400, "Patient ID is required");
        }
        if (request.getVisitId() == null) {
            throw new SDDException("visit", 400, "Visit ID is required");
        }
    }

    private void mapBasicVitalDetails(OpdPatientDetail opd, OpdPatientDetailCreateRequest request) {
        opd.setHeight(request.getHeight());
        opd.setIdealWeight(request.getIdealWeight());
        opd.setWeight(request.getWeight());
        opd.setPulse(request.getPulse());
        opd.setTemperature(request.getTemperature());
        opd.setRr(request.getRr());
        opd.setBmi(request.getBmi());
        opd.setSpo2(request.getSpo2());
        opd.setBpSystolic(request.getBpSystolic());
        opd.setBpDiastolic(request.getBpDiastolic());
        opd.setMlcFlag(request.getMlcFlag());
        opd.setPatient(request.getPatientId() != null ? patientRepository.findById(request.getPatientId()).orElseThrow(() -> new SDDException("patient", 404, "Patient not found")) : null);
        opd.setVisit(request.getVisitId() != null ? visitRepository.findById(request.getVisitId()).orElseThrow(() -> new SDDException("visit", 404, "Visit not found")) : null);
        opd.setDepartment(request.getDepartmentId() != null ? departmentRepository.findById(request.getDepartmentId()).orElseThrow(() -> new SDDException("department", 404, "Department not found")) : null);
        opd.setHospital(request.getHospitalId() != null ? hospitalRepository.findById(request.getHospitalId()).orElseThrow(() -> new SDDException("hospital", 404, "Hospital not found")) : null);
        opd.setDoctor(request.getDoctorId() != null ? userRepository.findById(request.getDoctorId()).orElseThrow(() -> new SDDException("doctor", 404, "Doctor not found")) : null);
        opd.setLastChgDate(Instant.now());
        opd.setLastChgBy(Objects.requireNonNull(getCurrentUser()).getFullName());
    }

    private void validateUpdateRequest(RecallOpdPatientDetailRequest request) {
        if (request == null) {
            throw new SDDException("request", 400, "Request cannot be null");
        }
        Objects.requireNonNull(request.getPatientId(), "OPD ID required");
    }

    private Patient getPatient(Long patientId) {
        return patientRepository.findById(patientId).orElseThrow(() -> new SDDException("patient", 404, "Patient not found"));
    }

    private Visit getVisit(Long visitId) {
        return visitRepository.findById(visitId).orElseThrow(() -> new SDDException("visit", 404, "Visit not found"));
    }

    private OpdPatientDetail getOpdPatient(Long opdId) {
        return opdPatientDetailRepository.findById(opdId).orElseThrow(() -> new SDDException("opd", 404, "OPD detail not found"));
    }

    private void mapClinicalDetails(OpdPatientDetail opd, OpdPatientDetailCreateRequest request) {
        opd.setPastMedicalHistory(request.getPastMedicalHistory());
        opd.setFamilyHistory(request.getFamilyHistory());
        opd.setClinicalExamination(request.getClinicalExamination());
        opd.setPatientSignsSymptoms(request.getPatientSignsSymptoms());
        opd.setWorkingDiag(request.getWorkingDiagnosis());
        opd.setFinalMedicalAdvice(request.getDoctorRemarks());
        opd.setTreatmentAdvice(request.getTreatmentAdvice());

        if (request.getIcdDiagnosis() != null && !request.getIcdDiagnosis().isEmpty()) {
            String joinedNames = request.getIcdDiagnosis().stream().filter(Objects::nonNull).map(OpdPatientDetailCreateRequest.IcdDiagnosis::getIcdDiagnosisName).filter(Objects::nonNull).collect(Collectors.joining(","));
            opd.setIcdDiag(joinedNames);
        } else {
            opd.setIcdDiag(null);
        }
    }

    private void saveIcdDiagnosis(List<OpdPatientDetailCreateRequest.IcdDiagnosis> list, Long opdId, Long visitId, Long userId) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<DischargeIcdCode> entities = new ArrayList<>();
        for (OpdPatientDetailCreateRequest.IcdDiagnosis icd : list) {
            if (icd == null) continue;
            DischargeIcdCode entity = new DischargeIcdCode();
            entity.setIcdId(icd.getIcdId());
            entity.setVisitId(visitId);
            entity.setOpdPatientDetailsId(opdId);
            entity.setAddEditById(userId);
            entity.setAddEditDate(LocalDate.now());
            entity.setAddEditTime(LocalTime.now().toString());
            entities.add(entity);
        }
        dischargeIcdCodeRepository.saveAll(entities);
    }

    private void saveTreatments(List<OpdPatientDetailCreateRequest.Treatment> list, Patient patient, Visit visit, User user, Long deptId) {
        if (list == null || list.isEmpty()) {
            return;
        }
        PatientPrescriptionHd hd = createPrescriptionHeader(patient, visit, user, deptId);
        List<PatientPrescriptionDt> details = new ArrayList<>();
        for (OpdPatientDetailCreateRequest.Treatment trt : list) {
            if (trt == null) continue;
            PatientPrescriptionDt dt = new PatientPrescriptionDt();
            dt.setPrescriptionHdId(hd.getPrescriptionHdId());
            dt.setItemId(trt.getItemId());
            dt.setDosage(trt.getDosage());
            dt.setFrequency(trt.getFrequency());
            dt.setDays(trt.getDays());
            dt.setTotal(trt.getTotal());
            dt.setInstruction(trt.getInstraction());
            dt.setStatus(AppConstants.STATUS_N.toLowerCase());
            details.add(dt);
        }
        patientPrescriptionDtRepository.saveAll(details);
    }

    private void mapGeneralDetails(OpdPatientDetail opd, Patient patient, Visit visit, User user, Long deptId) {
        opd.setPatient(patient);
        opd.setVisit(visit);
        opd.setOpdDate(Instant.now());
        opd.setHospital(user.getHospital());
        opd.setDoctor(user);
        opd.setDepartment(departmentRepository.findById(deptId).orElseThrow(() -> new SDDException("department", 404, "Department not found")));
        opd.setLastChgBy(user.getUsername());
        opd.setLastChgDate(Instant.now());


    }

    private PatientPrescriptionHd createPrescriptionHeader(Patient patient, Visit visit, User user, Long deptId) {
        PatientPrescriptionHd hd = new PatientPrescriptionHd();
        hd.setHospitalId(user.getHospital().getId());
        hd.setPatientId(patient.getId());
        hd.setDepartmentId(deptId);
        hd.setDoctorName(user.getFirstName());
        hd.setPrescriptionDate(LocalDateTime.now());
        hd.setStatus(AppConstants.STATUS_N.toLowerCase());
        hd.setCreatedBy(user.getFirstName());
        hd.setTotalCost(BigDecimal.ZERO);
        hd.setTotalGst(BigDecimal.ZERO);
        hd.setTotalDiscount(BigDecimal.ZERO);
        hd.setNetAmount(BigDecimal.ZERO);
        hd.setPrescriptionNumber(transactionSequenceService.generateTransactionNumber(HMISTransaction.PRESCRIPTION_NO, user.getHospital().getId()));
        hd.setVisit(visit);

        String medicineBilling = user.getHospital().getMedicineBilling();
        if (medicineBilling != null && AppConstants.PAYMENT_NOT_PAID.toLowerCase().equalsIgnoreCase(medicineBilling)) {
            hd.setBillingStatus(AppConstants.STATUS_N.toLowerCase());
        } else {
            hd.setBillingStatus(AppConstants.STATUS_Y.toLowerCase());
        }

        return patientPrescriptionHdRepository.save(hd);
    }

    private void closeVisit(Visit visit) {
        if (visit != null) {
            visit.setVisitStatus(AppConstants.VISIT_STATUS_COMPLETED.toLowerCase());
            visit.setDoctor(authUtil.getCurrentUser());
            visit.setDoctorName(authUtil.getCurrentUser().getFullName());
            visitRepository.save(visit);
            log.info("Closed visit with ID: {}", visit.getId());
        }
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public ApiResponse<String> updateRecallOpdPatientDetail(RecallOpdPatientDetailRequest request) {
        validateUpdateRequest(request);
        User user = getCurrentUser();
        if (user == null) {
            throw new SDDException("user", 401, "Current user not found or not authenticated");
        }
        Patient patient = getPatient(request.getPatientId());
        Visit visit = getVisit(request.getVisitId());
        OpdPatientDetail opd = getOpdPatient(request.getOpdPatientDetailId());
        mapRecallBasicDetails(opd, request);
        mapRecallClinicalDetails(opd, request);
        handleRecallAdmission(opd, request);
        handleRecallFollowUp(opd, request);
        handleRecallReferral(opd, request);
        opdPatientDetailRepository.save(opd);
        handleRecallPregnancyDetails(opd, request.getPregnancyDetails(), user);

        //ICD diagnosis details{
        List<Long> icdIds = Optional.ofNullable(request.getIcdDiagnosisList())
                .orElse(Collections.emptyList())
                .stream()
                .map(RecallOpdPatientDetailRequest.IcdDiagnosis::getIcdId)
                .collect(Collectors.toList());
        saveOrUpdateIcdDiagnosis(icdIds, opd.getOpdPatientDetailsId(), visit.getId(), user.getUserId());
        //}

        //investigation details{
        replaceInvestigations(request, patient, visit, user);
        //    }
        //Treatment data {
        List<TreatmentData> treatments = request.getTreatments()
                .stream()
                .map(t -> new TreatmentData(
                        t.getPrescriptionDtId(),
                        t.getItemId(),
                        t.getDosage(),
                        t.getFrequencyName() != null
                                ? t.getFrequencyName()
                                : null,
                        t.getDays(),
                        t.getTotal() != null
                                ? BigDecimal.valueOf(t.getTotal())
                                : null,
                        t.getInstruction()
                ))
                .toList();

        saveOrUpdateTreatments(treatments,patient,visit,user,authUtil.getCurrentDepartmentId());
        //}

        //removing facing issue because of store calculate the stock
        //removeTreatments(request.getRemovedTreatmentIds());

        replacePsychiatryAssessment(request, patient, visit, user);

        //OBG details
        if (request.getOpdObgDetailsRequest() != null) {
            handleRecallObgDetails(request, patient, visit, user);
        }
        //
        //EarExamination details save and update
        if (request.getEntExaminationDetails() != null) {
            ApiResponse<String> response = opdEntDetailsService.createOrUpdateEntDetails(request.getVisitId(), request.getEntExaminationDetails());
            if (response == null || response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("ent", 500, response != null ? response.getMessage() : "Failed to save ENT details");
            }
        }
        // opthal details Save and update
        if (request.getOphthalmologyExaminationDetails() != null) {
            OpdOpthDetailsRequest opthRequest = request.getOphthalmologyExaminationDetails();
            opthRequest.setPatientId(patient.getId());
            opthRequest.setVisitId(visit.getId());
            ApiResponse<String> response = opdOpthDetailsService.opdVisionExaminationDetailsSaveOrUpdate(opthRequest);
            if (response == null || response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("ophthalmology", 500, response != null ? response.getMessage() : "Failed to save ophthalmology details");
            }
        }
        return ResponseUtils.createSuccessResponse("Patient updated successfully", new TypeReference<>() {
        });
    }

    private void saveOrUpdateIcdDiagnosis(
            List<Long> icdIds,
            Long opdId,
            Long visitId,
            Long userId) {

        dischargeIcdCodeRepository.deleteByOpdPatientDetailsId(opdId);
        if (icdIds == null || icdIds.isEmpty()) {
            return;
        }
        List<DischargeIcdCode> entities = new ArrayList<>();
        for (Long icdId : icdIds) {
            if (icdId == null) {
                continue;
            }
            DischargeIcdCode entity = new DischargeIcdCode();
            entity.setIcdId(icdId);
            entity.setVisitId(visitId);
            entity.setOpdPatientDetailsId(opdId);
            entity.setAddEditById(userId);
            entity.setAddEditDate(LocalDate.now());
            entity.setAddEditTime(LocalTime.now().toString());
            entities.add(entity);
        }
        if (!entities.isEmpty()) {
            dischargeIcdCodeRepository.saveAll(entities);
        }
    }

    private void replaceIcdDiagnosis(RecallOpdPatientDetailRequest request, OpdPatientDetail opd, User user) {
        dischargeIcdCodeRepository.deleteByOpdPatientDetailsId(opd.getOpdPatientDetailsId());
        if (request.getIcdDiagnosisList() != null && !request.getIcdDiagnosisList().isEmpty()) {
            List<DischargeIcdCode> entities = new ArrayList<>();
            for (RecallOpdPatientDetailRequest.IcdDiagnosis icd : request.getIcdDiagnosisList()) {
                if (icd == null) continue;
                DischargeIcdCode entity = new DischargeIcdCode();
                entity.setIcdId(icd.getIcdId());
                entity.setVisitId(request.getVisitId());
                entity.setOpdPatientDetailsId(opd.getOpdPatientDetailsId());
                entity.setAddEditById(user.getUserId());
                entity.setAddEditDate(LocalDate.now());
                entity.setAddEditTime(LocalTime.now().toString());
                entities.add(entity);
            }
            if (!entities.isEmpty()) {
                dischargeIcdCodeRepository.saveAll(entities);
            }
        }
    }


    private void saveOrUpdateTreatments(
            List<TreatmentData> treatments,
            Patient patient,
            Visit visit,
            User user,
            Long deptId) {

        if (treatments == null || treatments.isEmpty()) {
            return;
        }
        PatientPrescriptionHd hd = patientPrescriptionHdRepository.findByVisit_Id(visit.getId());
        if (hd == null) {
            hd = createPrescriptionHeader(patient, visit, user, deptId);
        }
        for (TreatmentData treatment : treatments) {
            if (treatment == null || treatment.itemId() == null) {
                continue;
            }
            PatientPrescriptionDt dt;
            if (treatment.prescriptionDtId() != null) {
                dt = patientPrescriptionDtRepository
                        .findById(treatment.prescriptionDtId())
                        .orElseThrow(() ->
                                new SDDException("prescriptionDtId",404,"Prescription treatment not found: "
                                                + treatment.prescriptionDtId()));
            } else {
                dt = new PatientPrescriptionDt();
                dt.setPrescriptionHdId(hd.getPrescriptionHdId());
                dt.setStatus(AppConstants.STATUS_N.toLowerCase());
            }
            dt.setItemId(treatment.itemId());
            dt.setDosage(treatment.dosage());
            dt.setFrequency(treatment.frequency());
            dt.setDays(treatment.days());
            dt.setTotal(treatment.total());
            dt.setInstruction(treatment.instruction());

            patientPrescriptionDtRepository.save(dt);
        }
    }

    private void removeTreatments(List<Long> removedTreatmentIds) {
        if (removedTreatmentIds == null || removedTreatmentIds.isEmpty()) {
            return;
        }
        patientPrescriptionDtRepository.deleteAllById(removedTreatmentIds);
        log.info("Removed prescription treatment IDs: {}",removedTreatmentIds);
    }

    @Transactional(rollbackFor = Exception.class)
    private void replaceInvestigations(RecallOpdPatientDetailRequest request, Patient patient, Visit visit, User user) {
        if (request.getInvestigations() == null || request.getInvestigations().isEmpty()) {
            log.info("No investigations provided in recall update - keeping existing investigations");
            return;
        }
        log.info("Replacing investigations for visit ID: {}", visit.getId());
        deleteAllLabOrders(visit);
        deleteAllRadiologyOrders(visit);
        if (!request.getInvestigations().isEmpty()) {
            createInvestigationsFromRequest(request, patient, visit, user);
        }
    }

    private void deleteAllLabOrders(Visit visit) {
        List<DgOrderHd> existingLabOrders = dgOrderHdRepo.findAllByVisitId(visit);
        if (existingLabOrders != null && !existingLabOrders.isEmpty()) {
            for (DgOrderHd order : existingLabOrders) {
                List<DgOrderDt> details = dgOrderDtRepo.findByOrderhdId(order);
                if (details != null && !details.isEmpty()) {
                    dgOrderDtRepo.deleteAll(details);
                }
                dgOrderHdRepo.delete(order);
            }
            log.info("Deleted {} existing lab orders for visit ID: {}", existingLabOrders.size(), visit.getId());
        }
    }

    private void deleteAllRadiologyOrders(Visit visit) {
        List<RadOrderHd> existingRadOrders = radOrderHdRepository.findAllByVisit_Id(visit.getId());
        if (existingRadOrders != null && !existingRadOrders.isEmpty()) {
            for (RadOrderHd order : existingRadOrders) {
                List<RadOrderDt> details = radOrderDtRepository.findByRadOrderhd(order);
                if (details != null && !details.isEmpty()) {
                    radOrderDtRepository.deleteAll(details);
                }
                radOrderHdRepository.delete(order);
            }
            log.info("Deleted {} existing radiology orders for visit ID: {}", existingRadOrders.size(), visit.getId());
        }
    }

    private void createInvestigationsFromRequest(RecallOpdPatientDetailRequest request, Patient patient, Visit visit, User user) {
        List<RecallOpdPatientDetailRequest.InvestigationRequest> labInvestigations = new ArrayList<>();
        List<RecallOpdPatientDetailRequest.InvestigationRequest> radiologyInvestigations = new ArrayList<>();

        for (RecallOpdPatientDetailRequest.InvestigationRequest inv : request.getInvestigations()) {
            if (inv == null || inv.getInvestigationId() == null || inv.getInvestigationDate() == null) continue;

            Long departmentId = helperUtils.getDepartmentFromInvestigation(inv.getInvestigationId());
            if (departmentId.equals(Long.valueOf(laboratoryDepartment))) {
                labInvestigations.add(inv);
            } else if (departmentId.equals(Long.valueOf(radiologyDepartment))) {
                radiologyInvestigations.add(inv);
            }
        }

        if (!labInvestigations.isEmpty()) {
            createLabInvestigations(labInvestigations, patient, visit, user);
        }

        if (!radiologyInvestigations.isEmpty()) {
            createRadiologyInvestigations(radiologyInvestigations, patient, visit, user);
        }
    }

    private void createLabInvestigations(List<RecallOpdPatientDetailRequest.InvestigationRequest> investigations, Patient patient, Visit visit, User user) {
        LabOrderTrackingStatus labOrderedStatus = labOrderTrackingStatusRepository.findById(orderedStatusId).orElseThrow(() -> new SDDException("status", 500, "Ordered status not found"));

        Map<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> groupedByDate = investigations.stream().filter(inv -> inv.getInvestigationDate() != null).collect(Collectors.groupingBy(RecallOpdPatientDetailRequest.InvestigationRequest::getInvestigationDate));

        for (Map.Entry<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> entry : groupedByDate.entrySet()) {
            LocalDate appointmentDate = entry.getKey();

            // Create order header
            DgOrderHd dgOrderHd = new DgOrderHd();
            dgOrderHd.setAppointmentDate(appointmentDate);
            dgOrderHd.setOrderDate(LocalDate.now());
            dgOrderHd.setOrderTime(Instant.now());
            dgOrderHd.setOrderNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.LAB_NO, user.getHospital().getId()));
            dgOrderHd.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
            dgOrderHd.setCollectionStatus(AppConstants.STATUS_N.toLowerCase());
            dgOrderHd.setPaymentStatus(AppConstants.PAYMENT_PAID.equalsIgnoreCase(user.getHospital().getLabBilling()) ? AppConstants.PAYMENT_NOT_PAID.toLowerCase() : AppConstants.PAYMENT_PAID.toLowerCase());
            dgOrderHd.setSource("OPD PATIENT");
            dgOrderHd.setDiscountId(1);
            dgOrderHd.setPatientId(patient);
            dgOrderHd.setDepartmentId(authUtil.getCurrentDepartmentId());
            dgOrderHd.setHospitalId(user.getHospital().getId());
            dgOrderHd.setVisitId(visit);
            dgOrderHd.setCreatedBy(user.getFirstName());
            dgOrderHd.setLastChgBy(user.getFirstName());
            dgOrderHd.setCreatedOn(LocalDate.now());
            dgOrderHd.setLastChgDate(LocalDate.now());
            dgOrderHd.setLastChgTime(LocalTime.now().toString());

            DgOrderHd savedOrderHd = dgOrderHdRepo.save(dgOrderHd);

            // Create order details
            for (RecallOpdPatientDetailRequest.InvestigationRequest inv : entry.getValue()) {
                DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(inv.getInvestigationId()).orElseThrow(() -> new SDDException("investigation", 404, "Investigation not found"));

                DgOrderDt dt = new DgOrderDt();
                dt.setOrderhdId(savedOrderHd);
                dt.setInvestigationId(invEntity);
                dt.setAppointmentDate(inv.getInvestigationDate());
                dt.setOrderQty(1);
                dt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
                dt.setBillingStatus(savedOrderHd.getPaymentStatus());
                dt.setCreatedBy(user.getFirstName());
                dt.setLastChgBy(user.getFirstName());
                dt.setCreatedon(Instant.now());
                dt.setLastChgDate(LocalDate.now());
                dt.setLastChgTime(LocalTime.now().toString());
                dt.setOrderTrackingStatus(labOrderedStatus);

                if (invEntity.getMainChargeCodeId() != null) {
                    dt.setMainChargecodeId(invEntity.getMainChargeCodeId().getChargecodeId());
                }
                if (invEntity.getSubChargeCodeId() != null) {
                    dt.setSubChargeid(invEntity.getSubChargeCodeId().getSubId());
                }

                dgOrderDtRepo.save(dt);
            }
        }
        log.info("Created {} lab investigations for visit ID: {}", investigations.size(), visit.getId());
    }

    private void createRadiologyInvestigations(List<RecallOpdPatientDetailRequest.InvestigationRequest> investigations, Patient patient, Visit visit, User user) {
        // Group by date
        Map<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> groupedByDate = investigations.stream().filter(inv -> inv.getInvestigationDate() != null).collect(Collectors.groupingBy(RecallOpdPatientDetailRequest.InvestigationRequest::getInvestigationDate));

        for (Map.Entry<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> entry : groupedByDate.entrySet()) {
            LocalDate appointmentDate = entry.getKey();

            // Create radiology order header
            RadOrderHd radOrderHd = new RadOrderHd();
            radOrderHd.setAppointmentDate(appointmentDate);
            radOrderHd.setPaymentStatus(AppConstants.PAYMENT_PAID.equalsIgnoreCase(user.getHospital().getRadioBilling()) ? AppConstants.PAYMENT_NOT_PAID.toLowerCase() : AppConstants.PAYMENT_PAID.toLowerCase());
            radOrderHd.setOrderDate(LocalDate.now());
            radOrderHd.setOrderTime(Instant.now());
            radOrderHd.setPatient(patient);
            radOrderHd.setVisit(visit);
            radOrderHd.setDepartment(visit.getDepartment());
            radOrderHd.setHospital(visit.getHospital());
            radOrderHd.setLastChgBy(user.getFirstName() + " " + user.getLastName());
            radOrderHd.setLastChgDate(Instant.now());
            radOrderHd.setCreatedon(Instant.now());
            radOrderHd.setCreatedby(user.getFirstName() + " " + user.getLastName());

            RadOrderHd savedRadOrderHd = radOrderHdRepository.save(radOrderHd);

            // Create radiology order details
            for (RecallOpdPatientDetailRequest.InvestigationRequest inv : entry.getValue()) {
                DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(inv.getInvestigationId()).orElseThrow(() -> new SDDException("investigation", 404, "Investigation not found"));

                RadOrderDt radOrderDt = new RadOrderDt();
                radOrderDt.setRadOrderhd(savedRadOrderHd);
                radOrderDt.setInvestigation(invEntity);
                radOrderDt.setOrderAccessionNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.RADIOLOGY_NO, user.getHospital().getId()));
                radOrderDt.setSubChargecode(invEntity.getSubChargeCodeId());
                radOrderDt.setAppointmentDate(inv.getInvestigationDate());
                radOrderDt.setLastChgBy(user.getFirstName() + " " + user.getLastName());
                radOrderDt.setCreatedby(user.getFirstName() + " " + user.getLastName());
                radOrderDt.setBillingStatus(savedRadOrderHd.getPaymentStatus());
                radOrderDt.setCreatedon(Instant.now());
                radOrderDt.setLastChgDate(Instant.now());
                radOrderDt.setStudyStatus(AppConstants.STATUS_N.toLowerCase());
                radOrderDt.setReportStatus(AppConstants.STATUS_N.toLowerCase());
                radOrderDt.setHl7MwlStatus(AppConstants.STATUS_N.toLowerCase());
                radOrderDt.setPacsCompletionStatus(AppConstants.STATUS_N.toLowerCase());
                radOrderDt.setOrderStatus(AppConstants.STATUS_Y.toLowerCase());

                radOrderDtRepository.save(radOrderDt);
            }
        }
        log.info("Created {} radiology investigations for visit ID: {}", investigations.size(), visit.getId());
    }

// ===================== TREATMENTS METHODS =====================

    private void replaceTreatments(RecallOpdPatientDetailRequest request, Patient patient, Visit visit, User user) {
        PatientPrescriptionHd existingHd = patientPrescriptionHdRepository.findByVisit_Id(visit.getId());
        if (existingHd != null) {
            List<PatientPrescriptionDt> existingDetails = patientPrescriptionDtRepository.findByPrescriptionHdId(existingHd.getPrescriptionHdId());
            if (existingDetails != null && !existingDetails.isEmpty()) {
                patientPrescriptionDtRepository.deleteAll(existingDetails);
            }
            patientPrescriptionHdRepository.delete(existingHd);
            log.info("Deleted existing prescription header and details for visit ID: {}", visit.getId());
        }

        // Step 2: Create new treatments from request
        if (request.getTreatments() != null && !request.getTreatments().isEmpty()) {
            createNewTreatments(request.getTreatments(), patient, visit, user);
        }
    }

    private void replacePsychiatryAssessment(RecallOpdPatientDetailRequest request, Patient patient, Visit visit, User user) {
        if (request.getPsychiatricDetailsRequests() != null && !request.getPsychiatricDetailsRequests().isEmpty()) {
            // Get or create OpdPatientDetail (needed for header)
            OpdPatientDetail opdPatientDetail = opdPatientDetailRepository
                    .findByVisit_Id(visit.getId())
                    .orElseThrow(() -> new SDDException("opdPatientDetail", 404,
                            "OPD patient detail not found for visit ID: " + visit.getId()));

            // Save or update using the improved method
            saveOrUpdatePsychiatricAssessment(
                    request.getPsychiatricDetailsRequests(),
                    request.getTopicId(),
                    visit,
                    opdPatientDetail,
                    user
            );

            log.info("Psychiatric assessment updated successfully for visit ID: {}", visit.getId());
        } else {
            // If details are null or empty, delete existing psychiatric assessment
            OpdPsychiatryAssessmentHeader existingHeader = opdPsychiatryAssessmentHeaderRepository
                    .findByVisit_Id(visit.getId())
                    .orElse(null);

            if (existingHeader != null) {
                deleteAllPsychiatricDetails(existingHeader);
                opdPsychiatryAssessmentHeaderRepository.delete(existingHeader);
                log.info("Deleted psychiatric assessment for visit ID: {}", visit.getId());
            }
        }
    }

    private void handleRecallObgDetails(RecallOpdPatientDetailRequest request, Patient patient, Visit visit, User user) {
        try {
            OpdObgDetailsRequest obgRequest = request.getOpdObgDetailsRequest();
            obgRequest.setPatientId(patient.getId());
            obgRequest.setVisitId(visit.getId());
            ApiResponse<String> response = opdObgDetailsService.createOrUpdateObgDetails(
                    request.getVisitId(),
                    obgRequest
            );
            if (response == null || response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("obg", 500,
                        response != null ? response.getMessage() : "Failed to update OBG details");
            }
            log.info("OBG details updated successfully for visit ID: {}", visit.getId());
        } catch (Exception e) {
            log.error("Error updating OBG details for visit ID: {}", visit.getId(), e);
            throw new SDDException("obg", 500, "Failed to update OBG details: " + e.getMessage());
        }
    }



    private void createNewTreatments(List<RecallOpdPatientDetailRequest.TreatmentRequest> treatments, Patient patient, Visit visit, User user) {
        PatientPrescriptionHd hd = new PatientPrescriptionHd();
        hd.setHospitalId(user.getHospital().getId());
        hd.setPatientId(patient.getId());
        hd.setDepartmentId(authUtil.getCurrentDepartmentId());
        hd.setDoctorName(user.getFirstName());
        hd.setPrescriptionDate(LocalDateTime.now());
        hd.setStatus(AppConstants.STATUS_N.toLowerCase());
        hd.setCreatedBy(user.getFirstName());
        hd.setTotalCost(BigDecimal.ZERO);
        hd.setTotalGst(BigDecimal.ZERO);
        hd.setTotalDiscount(BigDecimal.ZERO);
        hd.setNetAmount(BigDecimal.ZERO);
        hd.setVisit(visit);
        hd.setPrescriptionNumber(transactionSequenceService.generateTransactionNumber(HMISTransaction.PRESCRIPTION_NO, user.getHospital().getId()));

        PatientPrescriptionHd savedHd = patientPrescriptionHdRepository.save(hd);

        List<PatientPrescriptionDt> details = new ArrayList<>();
        for (RecallOpdPatientDetailRequest.TreatmentRequest trt : treatments) {
            if (trt == null) continue;

            PatientPrescriptionDt dt = new PatientPrescriptionDt();
            dt.setPrescriptionHdId(savedHd.getPrescriptionHdId());
            dt.setItemId(trt.getItemId());
            dt.setDosage(trt.getDosage());
            dt.setFrequency(String.valueOf(trt.getFrequencyId()));
            dt.setDays(trt.getDays());
            dt.setTotal(trt.getTotal() == null ? BigDecimal.ZERO : BigDecimal.valueOf(trt.getTotal()));
            dt.setInstruction(trt.getInstruction());
            dt.setStatus(AppConstants.STATUS_N.toLowerCase());
            details.add(dt);
        }

        if (!details.isEmpty()) {
            patientPrescriptionDtRepository.saveAll(details);
        }
        log.info("Created {} new treatments for visit ID: {}", treatments.size(), visit.getId());
    }

    private void mapRecallBasicDetails(OpdPatientDetail opd, RecallOpdPatientDetailRequest request) {
        opd.setHeight(request.getHeight());
        opd.setWeight(request.getWeight());
        opd.setTemperature(request.getTemperature());
        opd.setBpDiastolic(request.getBpDiastolic());
        opd.setBpSystolic(request.getBpSystolic());
        opd.setPulse(request.getPulse());
        opd.setBmi(request.getBmi());
        opd.setRr(request.getRr());
        opd.setSpo2(request.getSpo2());
    }

    private void mapRecallClinicalDetails(OpdPatientDetail opd, RecallOpdPatientDetailRequest request) {
        opd.setPatientSignsSymptoms(request.getPatientSignsSymptoms());
        opd.setClinicalExamination(request.getClinicalExamination());
        opd.setPastMedicalHistory(request.getPastMedicalHistory());
        opd.setFamilyHistory(request.getFamilyHistory());
        opd.setWorkingDiag(request.getWorkingDiagnosis());
        opd.setFinalMedicalAdvice(request.getDoctorRemarks());
        opd.setTreatmentAdvice(request.getTreatmentAdvice());

        if (request.getIcdDiagnosisList() != null && !request.getIcdDiagnosisList().isEmpty()) {
            String joinedNames = request.getIcdDiagnosisList().stream().filter(Objects::nonNull).map(RecallOpdPatientDetailRequest.IcdDiagnosis::getIcdDiagnosisName).filter(Objects::nonNull).collect(Collectors.joining(","));
            opd.setIcdDiag(joinedNames);
        } else {
            opd.setIcdDiag(null);
        }
    }

    private void handleAdmission(OpdPatientDetail opd, OpdPatientDetailCreateRequest request) {
        if (isYes(request.getAdmissionFlag())) {
            if (request.getAdmissionCareLevel() != null) {
                masCareLevelRepository.findById(request.getAdmissionCareLevel()).ifPresent(opd::setAdmissionCareLevel);
            }
            if (request.getAdmissionWardCategory() != null) {
                masWardCategoryRepository.findById(request.getAdmissionWardCategory()).ifPresent(opd::setAdmissionWardCategory);
            }
            if (request.getAdmissionWard() != null) {
                masDepartmentRepository.findById(request.getAdmissionWard()).ifPresent(opd::setAdmissionWard);
            }
            opd.setAdmissionFlag(AppConstants.STATUS_Y.toLowerCase());
            opd.setAdmissionAdvisedDate(request.getAdmissionAdvisedDate());
            opd.setAdmissionRemarks(request.getAdmissionRemarks());
            opd.setAdmissionPriority(request.getAdmissionPriority());
        } else {
            opd.setAdmissionFlag(AppConstants.STATUS_N.toLowerCase());
        }
    }

    private void handleFollowUp(OpdPatientDetail opd, OpdPatientDetailCreateRequest request) {
        if (isYes(request.getFollowUpFlag())) {
            opd.setFollowUpFlag(AppConstants.STATUS_Y.toLowerCase());
            opd.setFollowUpDays(request.getFollowUpDays());
            opd.setFollowUpDate(request.getFollowUpDate());
        } else {
            opd.setFollowUpFlag(AppConstants.STATUS_N.toLowerCase());
        }
    }

    private void handleReferral(OpdPatientDetail opd, OpdPatientDetailCreateRequest request) {
        opd.setReferralFlag(isYes(request.getReferralFlag()) ? AppConstants.STATUS_Y.toLowerCase() : AppConstants.STATUS_N.toLowerCase());
        opd.setReferralRemarks(request.getReferralRemarks());
        opd.setReferralDate(request.getReferralDate());
        opd.setReferTo(request.getReferTo());
        opd.setReferredHospitalName(request.getReferredHospitalName());
    }

    private void handleRecallAdmission(OpdPatientDetail opd, RecallOpdPatientDetailRequest request) {
        if (isYes(request.getAdmissionFlag())) {
            opd.setAdmissionFlag(AppConstants.STATUS_Y.toLowerCase());
            if (request.getAdmissionCareLevel() != null) {
                masCareLevelRepository.findById(request.getAdmissionCareLevel()).ifPresent(opd::setAdmissionCareLevel);
            }
            if (request.getAdmissionWardCategory() != null) {
                masWardCategoryRepository.findById(request.getAdmissionWardCategory()).ifPresent(opd::setAdmissionWardCategory);
            }
            if (request.getAdmissionWard() != null) {
                masDepartmentRepository.findById(request.getAdmissionWard()).ifPresent(opd::setAdmissionWard);
            }
            opd.setAdmissionAdvisedDate(request.getAdmissionAdvisedDate());
            opd.setAdmissionRemarks(request.getAdmissionRemarks());
            opd.setAdmissionPriority(request.getAdmissionPriority());
        } else {
            opd.setAdmissionFlag(AppConstants.STATUS_N.toLowerCase());
            opd.setAdmissionAdvisedDate(null);
            opd.setAdmissionRemarks(null);
            opd.setAdmissionPriority(null);
            opd.setAdmissionCareLevel(null);
            opd.setAdmissionWardCategory(null);
            opd.setAdmissionWard(null);
        }
    }

    private void handleRecallFollowUp(OpdPatientDetail opd, RecallOpdPatientDetailRequest request) {
        if (isYes(request.getFollowUpFlag())) {
            opd.setFollowUpFlag(AppConstants.STATUS_Y.toLowerCase());
            opd.setFollowUpDays(request.getFollowUpDays());
            opd.setFollowUpDate(request.getFollowUpDate());
        } else {
            opd.setFollowUpFlag(AppConstants.STATUS_N.toLowerCase());
            opd.setFollowUpDays(null);
            opd.setFollowUpDate(null);
        }
    }

    private void handleRecallReferral(OpdPatientDetail opd, RecallOpdPatientDetailRequest request) {

        boolean isReferral = isYes(request.getReferralFlag());

        opd.setReferralFlag(isReferral ? AppConstants.STATUS_Y.toLowerCase() : AppConstants.STATUS_N.toLowerCase());

        if (isYes(request.getReferralFlag())) {
            opd.setReferralFlag(AppConstants.STATUS_Y.toLowerCase());
            opd.setReferralRemarks(request.getReferralRemarks());
            opd.setReferralDate(request.getReferralDate());
            opd.setReferTo(request.getReferTo());
            opd.setReferredHospitalName(request.getReferredHospitalName());
        } else {
            opd.setReferralFlag(AppConstants.STATUS_N.toLowerCase());
            opd.setReferralRemarks(null);
            opd.setReferralDate(null);
            opd.setReferTo(null);
            opd.setReferredHospitalName(null);
        }
    }

    private void handleRecallPregnancyDetails(
            OpdPatientDetail opd,
            RecallOpdPatientDetailRequest.PregnancyDetails pregnancyDetails,
            User user
    ) {
        if (opd == null || pregnancyDetails == null || opd.getVisit() == null || opd.getPatient() == null) {
            return;
        }

        Long visitId = opd.getVisit().getId();
        OpdPatientPregnancyDetails pregnancyEntity = opdPatientPregnancyDetailsRepository
                .findByVisit_Id(visitId)
                .orElseGet(OpdPatientPregnancyDetails::new);

        pregnancyEntity.setVisit(opd.getVisit());
        pregnancyEntity.setPatient(opd.getPatient());
        pregnancyEntity.setIsPregnant(pregnancyDetails.getIsPregnant());
        pregnancyEntity.setLmpDate(pregnancyDetails.getLmpDate());
        pregnancyEntity.setEdd(pregnancyDetails.getEdd());
        pregnancyEntity.setCurrentEdd(pregnancyDetails.getCurrentEdd());
        pregnancyEntity.setGestationPeriod(pregnancyDetails.getGestationPeriod());
        pregnancyEntity.setLastChgDate(Instant.now());
        pregnancyEntity.setLastChgBy(user != null ? user.getFullName() : null);

        opdPatientPregnancyDetailsRepository.save(pregnancyEntity);
    }

    private boolean isYes(String flag) {
        return flag != null && flag.equalsIgnoreCase("y");
    }

    /**
     * Process LAB investigations for OPD patient
     * Creates DgOrderHd and DgOrderDt records
     */
    private void processLabInvestigations(Map<LocalDate, List<OpdPatientDetailCreateRequest.Investigation>> groupedByDate, Patient patient, Visit visit, User currentUser, Long deptId, String orderNum, LabOrderTrackingStatus labOrderedStatus) {
        log.info("Starting LAB investigation processing for patient ID: {}", patient.getId());

        for (Map.Entry<LocalDate, List<OpdPatientDetailCreateRequest.Investigation>> entry : groupedByDate.entrySet()) {
            LocalDate appointmentDate = entry.getKey();
            List<OpdPatientDetailCreateRequest.Investigation> investigations = entry.getValue();

            log.debug("Processing {} LAB investigations for date: {}", investigations.size(), appointmentDate);

            // Create lab order header
            DgOrderHd dgOrderHd = new DgOrderHd();
            dgOrderHd.setAppointmentDate(appointmentDate);
            dgOrderHd.setOrderDate(LocalDate.now());
            dgOrderHd.setOrderTime(Instant.now());
            dgOrderHd.setOrderNo(orderNum);
            dgOrderHd.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
            dgOrderHd.setCollectionStatus(AppConstants.STATUS_N.toLowerCase());
            dgOrderHd.setPaymentStatus(AppConstants.PAYMENT_PAID.equalsIgnoreCase(currentUser.getHospital().getLabBilling()) ? AppConstants.PAYMENT_NOT_PAID.toLowerCase() : AppConstants.PAYMENT_PAID.toLowerCase());
            dgOrderHd.setSource("OPD PATIENT");
            dgOrderHd.setDiscountId(1);
            dgOrderHd.setPatientId(patient);
            dgOrderHd.setDepartmentId(deptId);
            dgOrderHd.setHospitalId(currentUser.getHospital().getId());
            dgOrderHd.setVisitId(visit);
            dgOrderHd.setCreatedBy(currentUser.getFirstName());
            dgOrderHd.setLastChgBy(currentUser.getFirstName());
            dgOrderHd.setCreatedOn(LocalDate.now());
            dgOrderHd.setLastChgDate(LocalDate.now());
            dgOrderHd.setLastChgTime(LocalTime.now().toString());

            DgOrderHd savedOrderHd = dgOrderHdRepo.save(dgOrderHd);
            log.info("LAB Order Header saved - Order ID: {}", savedOrderHd.getId());
//          For Billing if need some change in this billing process
//
//            log.info("Creating Billing Header...");
//            BigDecimal totalAmount = BigDecimal.ZERO;
//            BigDecimal discountAmount = BigDecimal.ZERO;
//            BigDecimal taxAmount = BigDecimal.ZERO;
//
//            if (!investigations.isEmpty()) {
//                for (OpdPatientDetailCreateRequest.Investigation inv : investigations) {
//                    DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(inv.getId()).orElseThrow(() -> new RuntimeException("Investigation not found with ID: " + inv.getId()));
//
//                    BigDecimal amount = getInvestigationPrice(invEntity);
//                    totalAmount = totalAmount.add(amount);
//                }
//            }
//
//            BillingHeader billingHeader = new BillingHeader();
//            String billNo = randomNumGenerator.generateOrderNumber("OPD", true, true);
//            billingHeader.setBillNo(billNo);
//            billingHeader.setPatient(patient);
//            billingHeader.setVisit(visit);
//            billingHeader.setPatientDisplayName(patient.getPatientFn());
//            billingHeader.setPatientAge(ageCalculator(patient.getPatientDob()));
//            billingHeader.setPatientGender(patient.getPatientGender() != null ? patient.getPatientGender().getGenderName() : "");
//            billingHeader.setPatientAddress(patient.getPatientAddress1());
//            billingHeader.setHospital(currentUser.getHospital());
//            billingHeader.setHospitalName(currentUser.getHospital().getHospitalName());
//            billingHeader.setHospitalAddress(currentUser.getHospital().getAddress());
//            billingHeader.setHospitalMobileNo(currentUser.getHospital().getContactNumber());
//            billingHeader.setHospitalGstin(currentUser.getHospital().getGstnNo());
//            billingHeader.setReferredBy(visit.getDoctorName());
//            billingHeader.setBillingDate(Instant.now());
//            billingHeader.setPaymentStatus(AppConstants.STATUS_N.toLowerCase());
//            billingHeader.setVisit(visit);
//            billingHeader.setHdorder(savedOrderHd);
//            billingHeader.setTotalAmount(totalAmount);
//            billingHeader.setDiscountAmount(discountAmount);
//            billingHeader.setNetAmount(totalAmount.subtract(discountAmount).add(taxAmount));
//            billingHeader.setTaxTotal(taxAmount);
//            billingHeader.setCreatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
//            billingHeader.setCreatedDt(Instant.now());
//            billingHeader.setUpdatedDt(Instant.now());
//            billingHeader.setBillDate(OffsetDateTime.now());
//            billingHeader.setUpdatedAt(OffsetDateTime.now());
//
//            BillingHeader savedBillingHeader = billingHeaderRepository.save(billingHeader);
//            log.info("Billing Header created successfully - Bill ID: {}", savedBillingHeader.getId());


            // Create lab order details
            for (OpdPatientDetailCreateRequest.Investigation invObj : investigations) {
                if (invObj == null || invObj.getId() == null) {
                    log.warn("Skipping null investigation object");
                    continue;
                }

                DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(invObj.getId()).orElseThrow(() -> new SDDException("investigation", 404, "Investigation not found with ID: " + invObj.getId()));

                if (invEntity.getMainChargeCodeId() == null || invEntity.getSubChargeCodeId() == null) {
                    throw new SDDException("chargeCode", 400, "Charge codes not configured for investigation ID: " + invObj.getId());
                }

                DgOrderDt dgOrderDt = new DgOrderDt();
                dgOrderDt.setInvestigationId(invEntity);
                dgOrderDt.setOrderhdId(savedOrderHd);
                dgOrderDt.setAppointmentDate(invObj.getInvestigationDate());
                dgOrderDt.setOrderQty(1);
                dgOrderDt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
                dgOrderDt.setBillingStatus(savedOrderHd.getPaymentStatus());
                dgOrderDt.setCreatedBy(currentUser.getFirstName());
                dgOrderDt.setLastChgBy(currentUser.getFirstName());
                dgOrderDt.setCreatedon(Instant.now());
                dgOrderDt.setLastChgDate(LocalDate.now());
                dgOrderDt.setMainChargecodeId(invEntity.getMainChargeCodeId().getChargecodeId());
                dgOrderDt.setSubChargeid(invEntity.getSubChargeCodeId().getSubId());
                dgOrderDt.setOrderTrackingStatus(labOrderedStatus);
                dgOrderDt.setLastChgTime(LocalTime.now().toString());

                DgOrderDt savedOrderDt = dgOrderDtRepo.save(dgOrderDt);
                log.debug("LAB Order Detail saved - Detail ID: {}", savedOrderDt.getId());
            }
        }
        log.info("LAB investigations processing completed");
    }

    /**
     * Process RADIOLOGY investigations for OPD patient
     * Creates RadOrderHd and RadOrderDt records with billing
     */
    private void processRadiologyInvestigations(Map<LocalDate, List<OpdPatientDetailCreateRequest.Investigation>> groupedByDate, Patient patient, Visit visit, User currentUser) {
        log.info("Starting RADIOLOGY investigation processing for patient ID: {}", patient.getId());
        MasServiceCategory radiologyServiceCategory = masServiceCategoryRepository.findByServiceCateCode("SC004");
        if (radiologyServiceCategory == null) {
            log.error("Radiology service category (SC004) not found");
            throw new SDDException("serviceCategory", 400, "Radiology service category not configured");
        }

        for (Map.Entry<LocalDate, List<OpdPatientDetailCreateRequest.Investigation>> entry : groupedByDate.entrySet()) {
            LocalDate appointmentDate = entry.getKey();
            List<OpdPatientDetailCreateRequest.Investigation> investigations = entry.getValue();

            log.debug("Processing {} RADIOLOGY investigations for date: {}", investigations.size(), appointmentDate);

            // Calculate totals for radiology investigations
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal discountAmount = BigDecimal.ZERO;
            BigDecimal taxAmount = BigDecimal.ZERO;

            for (OpdPatientDetailCreateRequest.Investigation inv : investigations) {
                if (inv.getId() != null) {
                    DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(inv.getId()).orElse(null);
                    if (invEntity != null) {
                        BigDecimal price = getInvestigationPrice(invEntity);
                        totalAmount = totalAmount.add(price);

                        // Calculate tax if applicable
                        if (radiologyServiceCategory.getGstApplicable()) {
                            taxAmount = taxAmount.add(price.multiply(BigDecimal.valueOf(radiologyServiceCategory.getGstPercent())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP));
                        }
                    }
                }
            }

            log.debug("Radiology totals - Amount: {}, Discount: {}, Tax: {}", totalAmount, discountAmount, taxAmount);

            // Create radiology order header
            RadOrderHd radOrderHd = new RadOrderHd();
            radOrderHd.setAppointmentDate(appointmentDate);
            radOrderHd.setPaymentStatus(AppConstants.PAYMENT_PAID.equalsIgnoreCase(currentUser.getHospital().getRadioBilling()) ? AppConstants.PAYMENT_NOT_PAID.toLowerCase() : AppConstants.PAYMENT_PAID.toLowerCase());
            radOrderHd.setOrderDate(LocalDate.now());
            radOrderHd.setOrderTime(Instant.now());
            radOrderHd.setPatient(patient);
            radOrderHd.setVisit(visit);
            radOrderHd.setDepartment(visit.getDepartment());
            radOrderHd.setHospital(visit.getHospital());
            radOrderHd.setLastChgBy(currentUser.getFirstName() + " " + currentUser.getLastName());
            radOrderHd.setLastChgDate(Instant.now());
            radOrderHd.setCreatedon(Instant.now());
            radOrderHd.setCreatedby(currentUser.getFirstName() + " " + currentUser.getLastName());

            RadOrderHd savedRadOrderHd = radOrderHdRepository.save(radOrderHd);
            log.info("RADIOLOGY Order Header saved - Order ID: {}", savedRadOrderHd.getId());

//            // Create radiology billing header
//            BillingHeader billingHeader = new BillingHeader();
//            String billNo = randomNumGenerator.generateOrderNumber("RAD", true, true);
//            billingHeader.setBillNo(billNo);
//            billingHeader.setPatient(patient);
//            billingHeader.setVisit(visit);
//            billingHeader.setPatientDisplayName(patient.getFullName());
//            billingHeader.setPatientAge(ageCalculator(patient.getPatientDob()));
//            billingHeader.setPatientGender(patient.getPatientGender() != null ? patient.getPatientGender().getGenderName() : "");
//            billingHeader.setPatientAddress(patient.getPatientAddress1());
//            billingHeader.setHospital(currentUser.getHospital());
//            billingHeader.setHospitalName(currentUser.getHospital().getHospitalName());
//            billingHeader.setHospitalAddress(currentUser.getHospital().getAddress());
//            billingHeader.setHospitalMobileNo(currentUser.getHospital().getContactNumber());
//            billingHeader.setHospitalGstin(currentUser.getHospital().getGstnNo());
//            billingHeader.setServiceCategory(radiologyServiceCategory);
//            billingHeader.setReferredBy(visit.getDoctorName());
//            billingHeader.setBillingDate(Instant.now());
//            billingHeader.setPaymentStatus(AppConstants.STATUS_N.toLowerCase());
//            billingHeader.setVisit(visit);
//            billingHeader.setRadOrderHd(savedRadOrderHd);
//            billingHeader.setTotalAmount(totalAmount);
//            billingHeader.setDiscountAmount(discountAmount);
//            billingHeader.setNetAmount(totalAmount.subtract(discountAmount).add(taxAmount));
//            billingHeader.setTaxTotal(taxAmount);
//            billingHeader.setCreatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
//            billingHeader.setCreatedDt(Instant.now());
//            billingHeader.setUpdatedDt(Instant.now());
//            billingHeader.setBillDate(OffsetDateTime.now());
//            billingHeader.setUpdatedAt(OffsetDateTime.now());
//
//            BillingHeader savedBillingHeader = billingHeaderRepository.save(billingHeader);
//            log.info("RADIOLOGY Billing Header created - Bill ID: {}", savedBillingHeader.getId());

            // Create radiology order and billing details
            for (OpdPatientDetailCreateRequest.Investigation invObj : investigations) {
                if (invObj == null || invObj.getId() == null) {
                    log.warn("Skipping null radiology investigation object");
                    continue;
                }

                DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(invObj.getId()).orElseThrow(() -> new SDDException("investigation", 404, "Investigation not found with ID: " + invObj.getId()));

                // Get investigation price
                BigDecimal chargeAmount = getInvestigationPrice(invEntity);

                // Create radiology order detail
                RadOrderDt radOrderDt = new RadOrderDt();
                radOrderDt.setRadOrderhd(savedRadOrderHd);
                radOrderDt.setInvestigation(invEntity);
                radOrderDt.setOrderAccessionNo(transactionSequenceService.generateTransactionNumber(HMISTransaction.RADIOLOGY_NO, currentUser.getHospital().getId()));
                radOrderDt.setSubChargecode(invEntity.getSubChargeCodeId());
                radOrderDt.setAppointmentDate(invObj.getInvestigationDate());
                radOrderDt.setLastChgBy(currentUser.getFirstName() + " " + currentUser.getLastName());
                radOrderDt.setCreatedby(currentUser.getFirstName() + " " + currentUser.getLastName());
                radOrderDt.setBillingStatus(savedRadOrderHd.getPaymentStatus());
                radOrderDt.setCreatedon(Instant.now());
                radOrderDt.setLastChgDate(Instant.now());
                radOrderDt.setBillingHd(null); // Set billing header if needed
                radOrderDt.setStudyStatus(AppConstants.STATUS_N.toLowerCase());
                radOrderDt.setReportStatus(AppConstants.STATUS_N.toLowerCase());
                radOrderDt.setHl7MwlStatus(AppConstants.STATUS_N.toLowerCase());
                radOrderDt.setPacsCompletionStatus(AppConstants.STATUS_N.toLowerCase());
                radOrderDt.setOrderStatus(AppConstants.STATUS_Y.toLowerCase());

                RadOrderDt savedRadOrderDt = radOrderDtRepository.save(radOrderDt);
                log.debug("RADIOLOGY Order Detail saved - Detail ID: {}", savedRadOrderDt.getId());

//                // Create radiology billing detail
//                BillingDetail billingDetail = new BillingDetail();
//                billingDetail.setBillingHd(savedBillingHeader);
//                billingDetail.setBillHd(savedBillingHeader);
//                billingDetail.setServiceCategory(radiologyServiceCategory);
//                billingDetail.setItemName(invEntity.getInvestigationName());
//                billingDetail.setQuantity(1);
//                billingDetail.setInvestigation(invEntity);
//                billingDetail.setCreatedDt(OffsetDateTime.now());
//                billingDetail.setUpdatedDt(OffsetDateTime.now());
//                billingDetail.setCreatedAt(Instant.now());
//                billingDetail.setBasePrice(chargeAmount);
//                billingDetail.setDiscount(BigDecimal.ZERO);
//                billingDetail.setTariff(chargeAmount);
//                billingDetail.setAmountAfterDiscount(chargeAmount);
//
//                // Calculate tax if applicable
//                BigDecimal detailTaxAmount = BigDecimal.ZERO;
//                if (radiologyServiceCategory.getGstApplicable()) {
//                    detailTaxAmount = chargeAmount.multiply(BigDecimal.valueOf(radiologyServiceCategory.getGstPercent())).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
//                }
//
//                billingDetail.setTaxAmount(detailTaxAmount);
//                billingDetail.setTaxPercent(BigDecimal.valueOf(radiologyServiceCategory.getGstPercent()));
//                billingDetail.setNetAmount(chargeAmount.add(detailTaxAmount));
//                billingDetail.setTotal(chargeAmount.add(detailTaxAmount));
//                billingDetail.setServiceId(radiologyServiceCategory.getId());
//                billingDetail.setPaymentStatus(AppConstants.STATUS_N.toLowerCase());
//                billingDetail.setChargeCost(chargeAmount);
//
//                BillingDetail savedBillingDetail = billingDetailRepository.save(billingDetail);
//                log.debug("RADIOLOGY Billing Detail saved - Detail ID: {}", savedBillingDetail.getId());


            }

            // Update visit with billing header
//            visit.setBillingHd(savedBillingHeader);
            visitRepository.save(visit);
        }

        log.info("RADIOLOGY investigations processing completed");
    }

    /**
     * Gets the current investigation price from investigation_price_details table by investigation ID
     * Tries multiple strategies:
     * 1. Get active price for current date
     * 2. Get latest price if no active price found
     * 3. Fallback to investigation's direct price field
     *
     * @param investigation DgMasInvestigation entity
     * @return BigDecimal price, or BigDecimal.ZERO if not found
     */
    private BigDecimal getInvestigationPrice(DgMasInvestigation investigation) {
        if (investigation == null) {
            return BigDecimal.ZERO;
        }
        LocalDate today = LocalDate.now();
        Optional<MasInvestigationPriceDetails> priceDetail = masInvestigationPriceDetailsRepository.findActivePriceByInvestigationAndDate(investigation, today);

        if (priceDetail.isPresent() && priceDetail.get().getPrice() != null) {
            log.debug("Found active investigation price for investigation ID: {} - Price: {}", investigation.getInvestigationId(), priceDetail.get().getPrice());
            return priceDetail.get().getPrice();
        }
        Optional<MasInvestigationPriceDetails> latestPrice = masInvestigationPriceDetailsRepository.findTopByInvestigationOrderByFromDateDesc(investigation);
        if (latestPrice.isPresent() && latestPrice.get().getPrice() != null) {
            log.debug("Found latest investigation price for investigation ID: {} - Price: {}", investigation.getInvestigationId(), latestPrice.get().getPrice());
            return latestPrice.get().getPrice();
        }
        if (investigation.getPrice() != null) {
            log.debug("Using fallback investigation direct price for investigation ID: {} - Price: {}", investigation.getInvestigationId(), investigation.getPrice());
            return BigDecimal.valueOf(investigation.getPrice());
        }
        log.warn("No price found for investigation ID: {}", investigation.getInvestigationId());
        return BigDecimal.ZERO;
    }


    @Override
    public ApiResponse<List<OpdPatientDetailsWaitingresponce>> getActiveVisits() {

        List<Visit> activeVisits = visitRepository.findByVisitStatusAndBillingStatus("n", "y");
        List<OpdPatientDetailsWaitingresponce> responseList = new ArrayList<>();

        for (Visit v : activeVisits) {

            OpdPatientDetailsWaitingresponce res = new OpdPatientDetailsWaitingresponce();

            // Patient
            if (v.getPatient() != null) {
                res.setPatientId(v.getPatient().getId());
                res.setEmployeeNo(v.getPatient().getUhidNo());
                res.setMobileNo(v.getPatient().getPatientMobileNumber());
                res.setDob(v.getPatient().getPatientDob());
                res.setAge(v.getPatient().getPatientAge());

                String fullName = buildFullName(v.getPatient().getPatientFn(), v.getPatient().getPatientMn(), v.getPatient().getPatientLn());
                res.setPatientName(fullName);

                res.setGender(v.getPatient().getPatientGender() != null ? v.getPatient().getPatientGender().getGenderName() : null);

                res.setRelation(v.getPatient().getPatientRelation() != null ? v.getPatient().getPatientRelation().getRelationName() : null);
            }

            // Visit
            res.setVisitId(v.getId());
            res.setTokenNo(v.getTokenNo() != null ? String.valueOf(v.getTokenNo()) : null);

            // Department
            if (v.getDepartment() != null) {
                res.setDeptId(v.getDepartment().getId());
                res.setDeptName(v.getDepartment().getDepartmentName());
            }

            // Doctor
            if (v.getDoctor() != null) {
                res.setDocterId(v.getDoctor().getUserId());

                String docFullName = buildFullName(v.getDoctor().getFirstName(), v.getDoctor().getMiddleName(), v.getDoctor().getLastName());
                res.setDocterName(docFullName);
            } else {
                res.setDocterId(null);
                res.setDocterName(null);
            }

            // Hospital
            if (v.getHospital() != null) {
                res.setHospitalId(v.getHospital().getId());
            }

            // Session
            if (v.getSession() != null) {
                res.setSessionId(v.getSession().getId());
                res.setSessionName(v.getSession().getSessionName());
            }

            responseList.add(res);
        }

        return ResponseUtils.createSuccessResponse(responseList, new TypeReference<List<OpdPatientDetailsWaitingresponce>>() {
        });
    }

    @Override
    public ApiResponse<List<OpdPatientDetailsWaitingresponce>> getActiveVisitsWithFilters(ActiveVisitSearchRequest req) {
        try {
            LocalDate visitDate = req.getDate() != null ? req.getDate().atZone(ZoneId.systemDefault()).toLocalDate() : LocalDate.now();

            List<OpdPatientDetailsWaitingProjection> projections = visitRepository.findActiveVisitsWithFilters(req.getDoctorId(), req.getSessionId(), req.getEmployeeNo(), req.getPatientName(), visitDate, AppConstants.STATUS_N.toLowerCase(), AppConstants.STATUS_Y.toLowerCase());

            List<OpdPatientDetailsWaitingresponce> responseList = projections.stream().map(p -> {
                OpdPatientDetailsWaitingresponce res = new OpdPatientDetailsWaitingresponce();
                res.setPatientId(p.getPatientId());
                res.setEmployeeNo(p.getEmployeeNo());
                res.setMobileNo(p.getMobileNo());
                res.setDob(p.getDob());
                res.setAge(p.getAge());
                res.setDisplayPatientStatus(p.getDisplayPatientStatus());
                res.setVisitDate(p.getVisitDate());
                res.setPatientName(p.getPatientName());
                res.setGender(p.getGender());
                res.setRelation(p.getRelation());
                res.setVisitId(p.getVisitId());
                res.setTokenNo(p.getTokenNo());
                res.setDeptId(p.getDeptId());
                res.setDeptName(p.getDeptName());
                res.setDocterId(p.getDocterId());
                res.setDocterName(p.getDocterName());
                res.setHospitalId(p.getHospitalId());
                res.setSessionId(p.getSessionId());
                res.setSessionName(p.getSessionName());
                return res;
            }).collect(Collectors.toList());

            // Sorting
            responseList.sort(Comparator.comparingInt(res -> {
                try {
                    return res.getTokenNo() != null ? Integer.parseInt(res.getTokenNo()) : Integer.MAX_VALUE;
                } catch (Exception e) {
                    return Integer.MAX_VALUE;
                }
            }));
            return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
            });

        } catch (Exception ex) {
            log.error("Error while fetching active visits. req={}", req, ex);
            return ResponseUtils.createFailureResponse(Collections.emptyList(), "Failed to fetch active visits", HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<OpdPatientRecallResponce> getRecallVisit(Long visitId) {
        try {

            OpdPatientRecallResponce response = new OpdPatientRecallResponce();
            RecallPatientProjection basicData = visitRepository.getRecallBasicDetails(visitId);
            mapBasicDetails(response, basicData);
            OpdPatientDetail opdPatientObj = opdPatientDetailRepository.findTopByVisit_IdOrderByOpdPatientDetailsIdDesc(visitId);
            List<DgOrderHd> dgOrderHdList = safeList(dgOrderHdRepo.findAllByVisitId_Id(visitId));
            List<RadOrderHd> orderHdList = safeList(radOrderHdRepository.findAllByVisit_Id(visitId));
            PatientPrescriptionHd prescriptionHdObj = patientPrescriptionHdRepository.findByPatientIdAndVisitId(visitId);
            List<PatientPrescriptionDt> prescDtList = prescriptionHdObj != null ? safeList(patientPrescriptionDtRepository.findByPrescriptionHdId(prescriptionHdObj.getPrescriptionHdId())) : Collections.emptyList();
            // ================= ITEM IDS =================
            List<Long> itemIds = prescDtList.stream().filter(Objects::nonNull).map(PatientPrescriptionDt::getItemId).filter(Objects::nonNull).distinct().toList();
            // ================= BULK FETCH ITEMS =================
            Map<Long, MasStoreItem> itemMap = itemIds.isEmpty() ? Collections.emptyMap() : masStoreItemRepository.findAllByItemIds(itemIds).stream().collect(Collectors.toMap(MasStoreItem::getItemId, Function.identity()));
            // ================= OPD DETAILS =================
            if (opdPatientObj != null) {

                mapOpdDetails(response, opdPatientObj);
                response.setTreatmentAdvice(opdPatientObj.getTreatmentAdvice());
            }

            if (basicData != null) {
                response.setPregnancyDetails(
                        mapPregnancyDetails(
                                opdPatientPregnancyDetailsRepository.findByVisit_Id(visitId).orElse(null)
                        )
                );
            } else {
                response.setPregnancyDetails(null);
            }

            // ================= DG / RADIO =================
            response.setLabOrderHds(buildDgOrderHdList(dgOrderHdList));

            response.setRadOrderHds(buildRadOrderHdList(orderHdList));

            // ================= PRESCRIPTION HD =================
            if (prescriptionHdObj != null) {

                OpdPatientRecallResponce.NewDPatientPrescriptionHd hd = new OpdPatientRecallResponce.NewDPatientPrescriptionHd();

                hd.setPrescriptionHdId(prescriptionHdObj.getPrescriptionHdId());

                hd.setStatus(prescriptionHdObj.getStatus());

                hd.setPrescriptionDate(prescriptionHdObj.getPrescriptionDate());

                response.setPatientPrescriptionHd(hd);
            }

            // ================= PRESCRIPTION DT =================
            List<OpdPatientRecallResponce.NewDPatientPrescriptionDt> newDtList = new ArrayList<>();

            Long hospitalId = authUtil.getCurrentUser() != null && authUtil.getCurrentUser().getHospital() != null ? authUtil.getCurrentUser().getHospital().getId() : null;

            for (PatientPrescriptionDt dt : prescDtList) {

                if (dt == null) continue;

                OpdPatientRecallResponce.NewDPatientPrescriptionDt newDt = new OpdPatientRecallResponce.NewDPatientPrescriptionDt();
                newDt.setPrescriptionDtId(dt.getPrescriptionDtId());
                newDt.setPrescriptionHdId(dt.getPrescriptionHdId());
                newDt.setStatus(dt.getStatus());
                newDt.setDosage(dt.getDosage());
                newDt.setFrequency(dt.getFrequency());
                newDt.setDays(dt.getDays());
                newDt.setTotal(dt.getTotal());
                newDt.setInstraction(dt.getInstruction());
                newDt.setItemId(dt.getItemId());
                newDt.setFrequencyId(dt.getFrequency());

                // ================= ITEM DETAILS =================
                MasStoreItem item = itemMap.get(dt.getItemId());
                if (item != null) {
                    newDt.setItemName(item.getNomenclature());
                    newDt.setAdispQty(item.getAdispQty());

                    if (item.getDispUnit() != null) {
                        newDt.setDispUnit(item.getDispUnit().getUnitName());
                        newDt.setDepUnit(item.getDispUnit().getUnitName());
                    }
                    if (item.getItemClassId() != null) {
                        newDt.setItemClassId(item.getItemClassId().getItemClassId());
                    }
                }

                // ================= STOCK =================
                Long stocks = 0L;
                if (hospitalId != null && dt.getItemId() != null) {
                    Long stockVal = stockFound.getAvailableStocks(hospitalId, deptIdStore, dt.getItemId(), hospDefinedDays);

                    stocks = stockVal != null ? stockVal : 0L;
                }

                newDt.setStocks(stocks);
                newDtList.add(newDt);
            }

            response.setPatientPrescriptionDts(newDtList);

            // ================= FOLLOW UP =================
            if (opdPatientObj != null) {
                response.setFollowUpFlag(opdPatientObj.getFollowUpFlag());
                if (isYes(opdPatientObj.getFollowUpFlag())) {
                    response.setFollowUpDays(opdPatientObj.getFollowUpDays());
                    response.setFollowUpDate(opdPatientObj.getFollowUpDate());
                }

                // ================= REFERRAL =================
                response.setReferralFlag(opdPatientObj.getReferralFlag());

                if (isYes(opdPatientObj.getReferralFlag())) {
                    response.setReferralRemarks(opdPatientObj.getReferralRemarks());
                    response.setReferralDate(opdPatientObj.getReferralDate());
                }

                // ================= ADMISSION =================
                response.setAdmissionFlag(opdPatientObj.getAdmissionFlag());
                if (isYes(opdPatientObj.getAdmissionFlag())) {

                    response.setAdmissionRemarks(opdPatientObj.getAdmissionRemarks());
                    response.setAdmissionAdvisedDate(opdPatientObj.getAdmissionAdvisedDate());
                    response.setAdmissionPriority(opdPatientObj.getAdmissionPriority());

                    if (opdPatientObj.getAdmissionCareLevel() != null) {
                        response.setAdmissionCareLevel(opdPatientObj.getAdmissionCareLevel().getCareId());
                        response.setAdmissionCareLevelName(opdPatientObj.getAdmissionCareLevel().getCareLevelName());
                    }

                    if (opdPatientObj.getAdmissionWardCategory() != null) {
                        response.setAdmissionWardCategory(opdPatientObj.getAdmissionWardCategory().getId());
                        response.setAdmissionWardCategoryName(opdPatientObj.getAdmissionWardCategory().getCategoryName());
                    }

                    if (opdPatientObj.getAdmissionWard() != null) {
                        response.setAdmissionWard(opdPatientObj.getAdmissionWard().getId());
                        response.setAdmissionWardName(opdPatientObj.getAdmissionWard().getDepartmentName());
                    }
                }
            }
            return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {
            });
        } catch (Exception ex) {
            log.error("Error while fetching data : ", ex);
            return ResponseUtils.createFailureResponse(null, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private void mapBasicDetails(OpdPatientRecallResponce response, RecallPatientProjection data) {
        if (data == null) {
            return;
        }
        response.setPatientId(data.getPatientId());
        response.setVisitId(data.getVisitId());
        response.setPatientName(buildFullName(data.getPatientFn(), data.getPatientMn(), data.getPatientLn()));
        response.setMobileNo(data.getPatientMobileNumber());
        response.setGender(data.getGenderName());
        response.setRelation(data.getRelationName());
        response.setDob(data.getPatientDob());
        response.setAge(data.getPatientAge());
        response.setDeptId(data.getDepartmentId());
        response.setDeptName(data.getDepartmentName());
        response.setDocterId(data.getDoctorId());
        response.setDocterName(buildFullName(data.getDoctorFirstName(), data.getDoctorMiddleName(), data.getDoctorLastName()));
        response.setHospitalId(data.getHospitalId());
    }

    private boolean isEmpty(String v) {
        return v == null || v.trim().isEmpty();
    }

    private String safeString(String v) {
        return (v == null || v.trim().isEmpty()) ? "" : v.trim();
    }

    private <T> List<T> safeList(List<T> list) {
        return list != null ? list : Collections.emptyList();
    }

    private void mapOpdDetails(OpdPatientRecallResponce response, OpdPatientDetail opd) {

        if (response == null || opd == null) {
            return;
        }

        // ---------------- BASIC OPD ----------------
        response.setOpdPatientId(opd.getOpdPatientDetailsId());
        response.setOpdDate(opd.getOpdDate());
        response.setPastMedicalHistory(opd.getPastMedicalHistory());
        response.setFamilyHistory(opd.getFamilyHistory());
        response.setPatientSignsSymptoms(opd.getPatientSignsSymptoms());
        response.setClinicalExamination(opd.getClinicalExamination());
        response.setHeight(opd.getHeight());
        response.setIdealWeight(opd.getIdealWeight());
        response.setWeight(opd.getWeight());
        response.setPulse(opd.getPulse());
        response.setTemperature(opd.getTemperature());
        response.setRr(opd.getRr());
        response.setBmi(opd.getBmi());
        response.setSpo2(opd.getSpo2());
        response.setBpSystolic(opd.getBpSystolic());
        response.setBpDiastolic(opd.getBpDiastolic());
        response.setMlcFlag(opd.getMlcFlag());
        response.setWorkingDiag(opd.getWorkingDiag());
        response.setReferTo(opd.getReferTo());
        response.setReferredHospitalName(opd.getReferredHospitalName());

        // ---------------- FINAL MEDICAL ADVICE ----------------
        response.setDoctorRemarks(opd.getFinalMedicalAdvice());

        // ---------------- ADMISSION ----------------
        response.setAdmissionFlag(opd.getAdmissionFlag());

        if (isYes(opd.getAdmissionFlag())) {

            response.setAdmissionAdvisedDate(opd.getAdmissionAdvisedDate());
            response.setAdmissionRemarks(opd.getAdmissionRemarks());
            response.setAdmissionPriority(opd.getAdmissionPriority());

            if (opd.getAdmissionCareLevel() != null) {
                response.setAdmissionCareLevel(opd.getAdmissionCareLevel().getCareId());
                response.setAdmissionCareLevelName(opd.getAdmissionCareLevel().getCareLevelName());
            }

            if (opd.getAdmissionWardCategory() != null) {
                response.setAdmissionWardCategory(opd.getAdmissionWardCategory().getId());
                response.setAdmissionWardCategoryName(opd.getAdmissionWardCategory().getCategoryName());
            }

            if (opd.getAdmissionWard() != null) {
                response.setAdmissionWard(opd.getAdmissionWard().getId());
                response.setAdmissionWardName(opd.getAdmissionWard().getDepartmentName());
            }
        }

        // ---------------- FOLLOW UP ----------------
        response.setFollowUpFlag(opd.getFollowUpFlag());

        if (isYes(opd.getFollowUpFlag())) {
            response.setFollowUpDate(opd.getFollowUpDate());
            response.setFollowUpDays(opd.getFollowUpDays());
        }

        // ---------------- REFERRAL ----------------
        response.setReferralFlag(opd.getReferralFlag());

        // ---------------- ICD DIAGNOSIS ----------------
        if (opd.getIcdDiag() != null && !opd.getIcdDiag().isEmpty() && opd.getVisit() != null) {

            List<DischargeIcdCode> icdList = safeList(dischargeIcdCodeRepository.findByOpdPatientDetailsIdAndVisitId(opd.getOpdPatientDetailsId(), opd.getVisit().getId()));

            List<OpdPatientRecallResponce.IcdDiagnosis> newList = new ArrayList<>();

            for (DischargeIcdCode dis : icdList) {

                if (dis == null) continue;

                OpdPatientRecallResponce.IcdDiagnosis d = new OpdPatientRecallResponce.IcdDiagnosis();

                d.setId(dis.getDischargeIcdCodeId());
                d.setIcdId(dis.getIcdId());

                if (dis.getIcdId() != null) {
                    masIcdRepository.findById(dis.getIcdId()).ifPresent(masIcd -> d.setIcdDiagName(masIcd.getIcdCode() + " - " + masIcd.getIcdName()));
                }

                newList.add(d);
            }

            response.setIcdDiag(newList);
        }

        // ---------------- FLAGS ----------------
        response.setLabFlag(opd.getLabFlag());
        response.setRadioFlag(opd.getRadioFlag());
    }

    private OpdPatientRecallResponce.PregnancyDetails mapPregnancyDetails(
            OpdPatientPregnancyDetails pregnancyDetails
    ) {
        if (pregnancyDetails == null) {
            return null;
        }

        OpdPatientRecallResponce.PregnancyDetails response =
                new OpdPatientRecallResponce.PregnancyDetails();
        response.setIsPregnant(pregnancyDetails.getIsPregnant());
        response.setLmpDate(pregnancyDetails.getLmpDate());
        response.setEdd(pregnancyDetails.getEdd());
        response.setCurrentEdd(pregnancyDetails.getCurrentEdd());
        response.setGestationPeriod(pregnancyDetails.getGestationPeriod());
        return response;
    }

    private List<OpdPatientRecallResponce.LabOrderHd> buildDgOrderHdList(List<DgOrderHd> hdList) {

        List<OpdPatientRecallResponce.LabOrderHd> newHdList = new ArrayList<>();

        for (DgOrderHd hdObj : safeList(hdList)) {

            if (hdObj == null) continue;

            OpdPatientRecallResponce.LabOrderHd hd = new OpdPatientRecallResponce.LabOrderHd();

            hd.setOrderHdId(hdObj.getId());
            hd.setOrderDate(hdObj.getOrderDate());
            hd.setOrderNo(hdObj.getOrderNo());
            hd.setOrderStatus(hdObj.getOrderStatus());
            hd.setCollectionStatus(hdObj.getCollectionStatus());
            hd.setPaymentStatus(hdObj.getPaymentStatus());
            hd.setAppointmentDate(hdObj.getAppointmentDate());

            List<DgOrderDt> dtList = safeList(dgOrderDtRepo.findByOrderhdId(hdObj));

            List<OpdPatientRecallResponce.LabOrderDt> newDtList = new ArrayList<>();

            for (DgOrderDt dt : dtList) {

                if (dt == null) continue;

                OpdPatientRecallResponce.LabOrderDt nd = new OpdPatientRecallResponce.LabOrderDt();

                nd.setOrderDtId(dt.getId());
                nd.setOrderQty(dt.getOrderQty());
                nd.setOrderStatus(dt.getOrderStatus());
                nd.setAppointmentDate(dt.getAppointmentDate());
                nd.setBillingStatus(dt.getBillingStatus());

                // Investigation
                if (dt.getInvestigationId() != null) {
                    nd.setInvestigationId(dt.getInvestigationId().getInvestigationId());
                    nd.setInvestigationName(dt.getInvestigationId().getInvestigationName());
                }

                // Package
                nd.setPackageId(dt.getPackageId() != null ? dt.getPackageId().getPackId() : null);

                // Billing
                nd.setBillingHd(dt.getBillingHd() != null ? dt.getBillingHd().getBillingHdId() : null);

                newDtList.add(nd);
            }

            hd.setLabOrderDts(newDtList);
            newHdList.add(hd);
        }

        return newHdList;
    }

    private List<OpdPatientRecallResponce.RadOrderHd> buildRadOrderHdList(List<RadOrderHd> hdList) {

        List<OpdPatientRecallResponce.RadOrderHd> newHdList = new ArrayList<>();

        for (RadOrderHd hdObj : safeList(hdList)) {

            if (hdObj == null) continue;

            OpdPatientRecallResponce.RadOrderHd hd = new OpdPatientRecallResponce.RadOrderHd();

            hd.setOrderHdId(Math.toIntExact(hdObj.getId()));
            hd.setOrderDate(hdObj.getOrderDate());

            hd.setPaymentStatus(hdObj.getPaymentStatus());
            hd.setAppointmentDate(hdObj.getAppointmentDate());

            List<RadOrderDt> dtList = safeList(radOrderDtRepository.findByRadOrderhd(hdObj));

            List<OpdPatientRecallResponce.RadOrderDt> newDtList = new ArrayList<>();

            for (RadOrderDt dt : dtList) {
                if (dt == null) continue;
                OpdPatientRecallResponce.RadOrderDt nd = new OpdPatientRecallResponce.RadOrderDt();
                nd.setOrderDtId(Math.toIntExact(dt.getId()));

                nd.setOrderStatus(dt.getOrderStatus());
                nd.setAppointmentDate(dt.getAppointmentDate());
                nd.setBillingStatus(dt.getBillingStatus());

                // Investigation
                if (dt.getInvestigation() != null) {
                    nd.setInvestigationId(dt.getInvestigation().getInvestigationId());
                    nd.setInvestigationName(dt.getInvestigation().getInvestigationName());
                }

                // Package
                nd.setPackageId(dt.getPackageId() != null ? dt.getPackageId().getPackId() : null);

                // Billing
                nd.setBillingHd(dt.getBillingHd() != null ? dt.getBillingHd().getBillingHdId() : null);

                newDtList.add(nd);
            }

            hd.setRadOrderDts(newDtList);
            newHdList.add(hd);
        }

        return newHdList;
    }


    //    update status
    @Override
    public ApiResponse<String> updateVisitStatus(Long visitId, String status) {

        Visit visit = visitRepository.findById(visitId).orElseThrow(() -> new RuntimeException("Visit not found with id: " + visitId));

        visit.setVisitStatus(status);

        visitRepository.save(visit);

        return ResponseUtils.createSuccessResponse("Status updated successfully", new TypeReference<>() {
        });
    }


    private String buildFullName(String firstName, String middleName, String lastName) {
        StringBuilder name = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) {
            name.append(firstName.trim());
        }
        if (middleName != null && !middleName.trim().isEmpty()) {
            if (name.length() > 0) name.append(" ");
            name.append(middleName.trim());
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (name.length() > 0) name.append(" ");
            name.append(lastName.trim());
        }
        return name.toString().trim();
    }


    @Transactional
    @Override
    public Visit updateVisitStatus(Long visitId, Instant visitDate, Long doctorId) {

        // Fetch current visit
        Visit currentVisit = visitRepository.findById(visitId).orElseThrow(() -> new RuntimeException("Visit not found"));

        // STEP 1 — Find previous CP visit
        Optional<Visit> cpVisitOpt = visitRepository.findCpVisit(doctorId, visitDate, "cp");

        if (cpVisitOpt.isPresent()) {
            Visit cpVisit = cpVisitOpt.get();

            // Case A: Completed → NP
            if ("c".equalsIgnoreCase(cpVisit.getVisitStatus())) {
                cpVisit.setDisplayPatientStatus("np");
                visitRepository.save(cpVisit);
            }
            // Case B: NOT completed → WP
            else {
                cpVisit.setDisplayPatientStatus("wp");
                visitRepository.save(cpVisit);
            }
        }

        // STEP 2 — Set CURRENT visit as CP
        currentVisit.setDisplayPatientStatus("cp");
        visitRepository.save(currentVisit);

        // STEP 3 — Fetch NEXT visits
        List<Visit> nextVisits = visitRepository.findNextVisits(doctorId, visitDate, currentVisit.getTokenNo());

        boolean rpAssigned = false;

        for (Visit next : nextVisits) {

            boolean validStatus = "n".equalsIgnoreCase(next.getVisitStatus());
            boolean validBilling = "y".equalsIgnoreCase(next.getBillingStatus());

            if (!rpAssigned && validStatus && validBilling) {
                // First valid next → RP
                next.setDisplayPatientStatus("rp");
                visitRepository.save(next);
                rpAssigned = true;
            } else {
                // Everything else → WP
                next.setDisplayPatientStatus("wp");
                visitRepository.save(next);
            }
        }

        return currentVisit;
    }


    @Override
    public ApiResponse<Page<OpdPreConsultationResponse>> getPendingPreConsultations(Pageable pageable, String patientName, String mobileNumber) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getHospital() == null) {
                Page<OpdPreConsultationResponse> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
                return ResponseUtils.createSuccessResponse(emptyPage, new TypeReference<>() {
                });
            }
            Long hospitalId = currentUser.getHospital().getId();
            Long departmentId = authUtil.getCurrentDepartmentId();
            Page<OpdPreConsultationProjection> projectionPage = visitRepository.findPendingPreConsultationsByHospitalPaged(hospitalId, departmentId, AppConstants.STATUS_N.toLowerCase(), AppConstants.STATUS_Y.toLowerCase(), AppConstants.STATUS_N.toLowerCase(), patientName, mobileNumber, pageable);
            Page<OpdPreConsultationResponse> responsePage = projectionPage.map(this::mapOpdPreConsultationProjectionToResponse);
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error fetching pending pre-consultations: ", e);
            return ResponseUtils.createFailureResponse(new PageImpl<>(new ArrayList<>(), pageable, 0), new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    /**
     * Retrieves the patient waiting list for the current hospital.
     *
     * @return ApiResponse containing list of patients in waiting list
     */
    @Override
    public ApiResponse<Page<PatientWaitingListResponse>> getWaitingList(Pageable pageable, String patientName, String mobileNumber, Long doctorId, Long sessionId) {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null || currentUser.getHospital() == null) {
                return ResponseUtils.createFailureResponse(new PageImpl<>(new ArrayList<>(), pageable, 0), new TypeReference<>() {
                }, "User or hospital not found", 400);
            }

            Long hospitalId = currentUser.getHospital().getId();
            Long departmentId = authUtil.getCurrentDepartmentId();
            Page<PatientWaitingListProjection> projectionPage = visitRepository.findWaitingPatientsByHospitalWithFilters(hospitalId, departmentId, AppConstants.STATUS_Y.toLowerCase(), AppConstants.STATUS_N.toLowerCase(), patientName, mobileNumber, doctorId, sessionId, pageable);

            Page<PatientWaitingListResponse> responsePage = projectionPage.map(this::mapPatientWaitingListProjectionToResponse);
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error fetching patient waiting list: ", e);
            return ResponseUtils.createFailureResponse(new PageImpl<>(new ArrayList<>(), pageable, 0), new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Page<PreviousOpdVisitResponse>> getPreviousOpdVisit(Long patientId, Long hospitalId, int page, int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("visitDate").descending());
            Page<PreviousOpdVisitProjection> projectionPage = visitRepository.getPreviousOpdVisit(patientId, hospitalId, AppConstants.STATUS_Y.toLowerCase(), pageable);

            //Projection → DTO
            Page<PreviousOpdVisitResponse> responsePage = projectionPage.map(p -> {
                PreviousOpdVisitResponse res = new PreviousOpdVisitResponse();
                res.setVisitDate(p.getVisitDate());
                res.setDoctorName(p.getDoctorName());
                res.setDepartment(p.getDepartment());
                res.setIcdDiag(p.getIcdDiag());
                res.setWorkingDiag(p.getWorkingDiag());
                res.setVisitId(p.getVisitId());
                return res;
            });

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<Page<PreviousOpdVisitResponse>>() {
            });

        } catch (Exception ex) {
            log.error("Error fetching getPriviousHistoryByPatient: ", ex);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Page<PreviousOpdVitalsDetailsResponse>> getPreviousOpdVitalsDetailsHistory(Long patientId, Long hospitalId, int page, int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("visitDate").descending());
            Page<PreviousOpdVitalsDetailsProjection> projectionPage = visitRepository.getPriviousOpdVitalsDetails(patientId, hospitalId, AppConstants.STATUS_Y.toLowerCase(), pageable);

            //Projection → DTO
            Page<PreviousOpdVitalsDetailsResponse> responsePage = projectionPage.map(p -> {
                PreviousOpdVitalsDetailsResponse res = new PreviousOpdVitalsDetailsResponse();
                res.setVisitDate(p.getVisitDate());
                res.setBmi(p.getBmi());
                res.setRr(p.getRr());
                res.setTemperature(p.getTemperature());
                res.setHeight(p.getHeight());
                res.setWeight(p.getWeight());
                res.setSpo2(p.getSpo2());
                res.setBpDiastolic(p.getBpDiastolic());
                res.setBpSystolic(p.getBpSystolic());
                res.setPulse(p.getPulse());
                return res;
            });

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<Page<PreviousOpdVitalsDetailsResponse>>() {
            });

        } catch (Exception ex) {
            log.error("Error fetching getPriviousHistoryByPatient: ", ex);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    public ApiResponse<Page<PreviousOpdPsychiatryHistoryResponse>> getPreviousOpdPsychiatryDetailsHistory(
            Long patientId,
            Long hospitalId,
            int page,
            int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "assessmentDate"));

            Page<OpdPsychiatryAssessmentHeader> headerPage = opdPsychiatryAssessmentHeaderRepository.findByPatient_IdOrderByAssessmentDateDesc(patientId, pageable);

            List<Long> headerIds = headerPage.getContent().stream().map(OpdPsychiatryAssessmentHeader::getAssessmentHeaderId).filter(Objects::nonNull).toList();

            Map<Long, Map<Long, List<PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse>>> detailsByHeaderId = new HashMap<>();
            Map<Long, Map<Long, String>> topicNamesByHeaderId = new HashMap<>();
            if (!headerIds.isEmpty()) {
                List<OpdPsychiatryAssessmentDetail> detailEntities = opdPsychiatryAssessmentDetailRepository.findByAssessmentHeaderId_AssessmentHeaderIdIn(headerIds);

                for (OpdPsychiatryAssessmentDetail detail : detailEntities) {
                    if (detail == null || detail.getAssessmentHeaderId() == null) continue;
                    PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse qResponse = mapPsychiatryDetailToResponse(detail);
                    Long headerId = detail.getAssessmentHeaderId().getAssessmentHeaderId();
                    Long topicId = null;
                    String topicName = null;
                    if (detail.getQuestionId() != null && detail.getQuestionId().getQuestionHeading() != null) {
                        topicId = detail.getQuestionId().getQuestionHeading().getQuestionHeadingId();
                        topicName = detail.getQuestionId().getQuestionHeading().getQuestionHeadingName();
                    }
                    detailsByHeaderId.computeIfAbsent(headerId, k -> new HashMap<>()).computeIfAbsent(topicId, k -> new ArrayList<>()).add(qResponse);
                    if (topicName != null) {
                        topicNamesByHeaderId.computeIfAbsent(headerId, k -> new HashMap<>()).putIfAbsent(topicId, topicName);
                    }
                }
            }



            Page<PreviousOpdPsychiatryHistoryResponse> responsePage = headerPage.map(header -> {
                PreviousOpdPsychiatryHistoryResponse response = new PreviousOpdPsychiatryHistoryResponse();
                response.setAssessmentHeaderId(header.getAssessmentHeaderId());
                response.setPatientId(header.getPatient() != null ? header.getPatient().getId() : null);
                response.setVisitId(header.getVisit() != null ? header.getVisit().getId() : null);
                response.setDoctorName(extractDoctorName(header));
                response.setAssessmentDate(header.getAssessmentDate());
                response.setTotalScore(header.getTotalScore());
                response.setRemarks(header.getRemarks());

                Map<Long, List<PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse>> topicsMap =
                        detailsByHeaderId.getOrDefault(header.getAssessmentHeaderId(), Collections.emptyMap());

                List<PreviousOpdPsychiatryHistoryResponse.PsychiatricAssessmentResponse> assessments = new ArrayList<>();
                for (Map.Entry<Long, List<PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse>> entry : topicsMap.entrySet()) {
                    PreviousOpdPsychiatryHistoryResponse.PsychiatricAssessmentResponse pa =
                            new PreviousOpdPsychiatryHistoryResponse.PsychiatricAssessmentResponse();
                    Long tId = entry.getKey();
                    String tName = null;
                    Map<Long, String> topicNames = topicNamesByHeaderId.getOrDefault(header.getAssessmentHeaderId(), Collections.emptyMap());
                    if (tId != null) {
                        tName = topicNames.getOrDefault(tId, null);
                    }
                    if (tName == null) {
                        tName = header.getTopic() != null ? header.getTopic().getQuestionHeadingName() : null;
                    }
                    pa.setTopicName(tName);
                    pa.setQuestionsResponses(entry.getValue());
                    assessments.add(pa);
                }

                response.setAssessments(assessments);
                return response;
            });

            return ResponseUtils.createSuccessResponse(
                    responsePage,
                    new TypeReference<Page<PreviousOpdPsychiatryHistoryResponse>>() {
                    });

        } catch (Exception ex) {
            log.error("Error fetching previous psychiatry history for patientId={}, hospitalId={}", patientId, hospitalId, ex);
            return ResponseUtils.createFailureResponse(
                    null,
                    new TypeReference<>() {
                    },
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<OpdRecallVisitResponse>> getRecallOpdVisit(String name, String mobile, LocalDate visitDate, int page, int size) {

        try {
            Long departmentId = authUtil.getCurrentDepartmentId();
            name = name == null ? "" : name.trim();
            mobile = mobile == null ? "" : mobile.trim();
            boolean dateFilter = visitDate != null;
            Instant startDate = null;
            Instant endDate = null;

            if (dateFilter) {
                startDate = visitDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                endDate = visitDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            }
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
            Page<OpdRecallVisitProjection> projectionPage = visitRepository.getOpdRecallVisit(name, mobile, dateFilter, startDate, endDate, departmentId, AppConstants.VISIT_STATUS_COMPLETED.toLowerCase(), pageable);
            Page<OpdRecallVisitResponse> responsePage = projectionPage.map(this::mapToResponse);
            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<>() {
            });
        } catch (Exception e) {
            log.error("Error while fetching OPD recall visit data : {}", e.getMessage(), e);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, AppConstants.INTERNAL_SERVER_ERR_MSG, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private String extractDoctorName(OpdPsychiatryAssessmentHeader header) {
        if (header == null || header.getVisit() == null) {
            return null;
        }

        if (header.getVisit().getDoctorName() != null && !header.getVisit().getDoctorName().isBlank()) {
            return header.getVisit().getDoctorName();
        }

        if (header.getVisit().getDoctor() != null) {
            return header.getVisit().getDoctor().getFullName();
        }

        return null;
    }

    private PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse mapPsychiatryDetailToResponse(OpdPsychiatryAssessmentDetail detail) {
        PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse response = new PreviousOpdPsychiatryHistoryResponse.AssessmentQuestionsResponse();
        if (detail == null) return response;
        response.setQuestionName(detail.getQuestionId() != null ? detail.getQuestionId().getQuestion() : null);
        response.setQuestionsAns(detail.getAnswerOptionId() != null ? detail.getAnswerOptionId().getOptionValue() : null);
        return response;
    }

    /**
     * Retrieves the currently authenticated user from the security context.
     *
     * @return User object or null if not found
     */
    private User getCurrentUser() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            User user = userRepository.findByUserName(username);
            if (user == null) {
                log.warn("User not found in database for username: {}", username);
            }
            return user;
        } catch (Exception e) {
            log.error("Error retrieving current user from security context", e);
            return null;
        }
    }

    /**
     * Maps OpdPreConsultationProjection to OpdPreConsultationResponse.
     *
     * @param projection the projection object to map
     * @return mapped response object
     */
    private OpdPreConsultationResponse mapOpdPreConsultationProjectionToResponse(OpdPreConsultationProjection projection) {
        OpdPreConsultationResponse response = new OpdPreConsultationResponse();
        response.setVisitId(projection.getVisitId());
        response.setPatientId(projection.getPatientId());
        response.setPatientName(projection.getPatientName());
        response.setAge(projection.getPatientAge());
        response.setGender(projection.getGender());
        response.setDepartmentId(String.valueOf(projection.getDepartmentId()));
        response.setDepartmentName(projection.getDepartmentName());
        response.setMobleNumber(projection.getMobileNumber());
        response.setVisitType(projection.getVisitType());
        response.setDoctorId(projection.getDoctorId());
        response.setDoctorName(projection.getDoctorName());
        response.setAppointmentDate(projection.getAppointmentDate() != null ? projection.getAppointmentDate().toString() : "");
        response.setAppointmentTime(projection.getAppointmentTime());
        response.setTokenNumber(String.valueOf(projection.getTokenNumber()));
        return response;
    }

    /**
     * Maps PatientWaitingListProjection to PatientWaitingListResponse.
     *
     * @param projection the projection object to map
     * @return mapped response object
     */
    private PatientWaitingListResponse mapPatientWaitingListProjectionToResponse(PatientWaitingListProjection projection) {
        PatientWaitingListResponse response = new PatientWaitingListResponse();
        response.setPatientId(projection.getPatientId());
        response.setVisitId(projection.getVisitId());
        response.setTokenNo(String.valueOf(projection.getTokenNo()));
        response.setMobileNo(projection.getMobileNumber());
        response.setPatientName(projection.getPatientName());
        response.setRelation(projection.getRelation());
        response.setAge(ageCalculator(projection.getDob()));
        response.setDob(projection.getDob());
        response.setGender(projection.getGender());
        response.setVisitType(projection.getVisitType());
        response.setDepartmentName(projection.getDepartmentName());
        response.setOpdDate(projection.getOpdDate());
        return response;
    }

    @Override
    public ApiResponse<List<PrescriptionDetailResponse>> getPrescriptionDetailsByPatientId(Long patientId) {
        try {
            log.info("Fetching prescription details for patient ID: {}", patientId);

            if (patientId == null || patientId <= 0) {
                log.warn("Invalid patient ID: {}", patientId);
                return ResponseUtils.createFailureResponse(new ArrayList<>(), new TypeReference<>() {
                }, "Patient ID is invalid", 400);
            }
            List<PrescriptionDetailProjection> prescriptionDetails = prescriptionDtRepository.findPrescriptionDetailsByPatientIdWithinLimitedDays(patientId, prescriptionHistoryDays);
            if (prescriptionDetails.isEmpty()) {
                log.info("No prescription details found for patient ID: {}", patientId);
                return ResponseUtils.createSuccessResponse(new ArrayList<>(), new TypeReference<>() {
                });
            }
            log.info("Found {} prescription detail items for patient ID: {}", prescriptionDetails.size(), patientId);

            List<PrescriptionDetailResponse> responses = prescriptionDetails.stream().map(this::mapPrescriptionDetailProjectionToResponse).collect(Collectors.toList());

            return ResponseUtils.createSuccessResponse(responses, new TypeReference<>() {
            });

        } catch (Exception e) {
            log.error("Error fetching prescription details for patient ID: {}", patientId, e);
            return ResponseUtils.createFailureResponse(new ArrayList<>(), new TypeReference<>() {
            }, "Error retrieving prescription details: " + e.getMessage(), 500);
        }
    }

    private PrescriptionDetailResponse mapPrescriptionDetailProjectionToResponse(PrescriptionDetailProjection projection) {
        return PrescriptionDetailResponse.builder().prescriptionDtId(projection.getPrescriptionDtId()).prescriptionHdId(projection.getPrescriptionHdId()).drugId(projection.getItemId()).drugName(projection.getItemName()).dosage(projection.getDosage()).frequency(projection.getFrequency()).days(projection.getDays()).total(projection.getTotal()).issuedQty(projection.getIssuedQty()).route(projection.getRoute()).instruction(projection.getInstruction()).unitPrice(projection.getUnitPrice()).discount(projection.getDiscount()).gstRate(projection.getGstRate()).lineCost(projection.getLineCost()).status(projection.getStatus()).batchNo(projection.getBatchNo()).expiryDate(projection.getExpiryDate()).doctorName(projection.getDoctorName()).departmentName(projection.getDepartmentName()).prescribedDate(projection.getPrescribedDate()).dispUnit(projection.getDispUnit()).build();
    }

    private OpdRecallVisitResponse mapToResponse(OpdRecallVisitProjection projection) {
        OpdRecallVisitResponse response = new OpdRecallVisitResponse();
        response.setVisitId(projection.getVisitId());
        response.setPatientId(projection.getPatientId());
        response.setPatientName(projection.getPatientName());
        response.setMobileNo(projection.getMobileNo());
        response.setGender(projection.getGender());
        response.setAge(projection.getAge());
        response.setDeptName(projection.getDeptName());
        response.setDoctorName(projection.getDoctorName());
        return response;
    }

    @Transactional
    public OpdPsychiatryAssessmentHeader saveOrUpdatePsychiatricAssessment(
            List<OpdPsychiatricDetailsRequest> details,
            Long topicId,
            Visit visit,
            OpdPatientDetail opdPatientDetail,
            User user) {

        // 1. Find or create header
        OpdPsychiatryAssessmentHeader header = opdPsychiatryAssessmentHeaderRepository
                .findByVisit_Id(visit.getId())
                .orElse(new OpdPsychiatryAssessmentHeader());

        boolean isNew = header.getAssessmentHeaderId() == null;

        // 2. Set header fields
        if (isNew) {
            header.setVisit(visit);
            header.setDepartment(visit.getDepartment());
            header.setPatient(visit.getPatient());
            header.setOpdPatientDetails(opdPatientDetail);
            header.setAssessmentDate(LocalDateTime.now());
//            header.setCreatedBy(user.getFullName());
//            header.setCreatedDate(LocalDateTime.now());
        } else {
            // Update existing header
//            header.setLastUpdatedBy(user.getFullName());
//            header.setLastUpdateDate(LocalDateTime.now());

            // Optionally update topic if changed
            if (topicId != null && !topicId.equals(header.getTopic().getQuestionHeadingId())) {
                header.setTopic(masQuestionHeadingRepository.findById(topicId)
                        .orElseThrow(() -> new EntityNotFoundException("Topic not found with id: " + topicId)));
            }
        }

        // 3. Save header first
        OpdPsychiatryAssessmentHeader savedHeader = opdPsychiatryAssessmentHeaderRepository.save(header);

        // 4. Handle details - Update or Create
        if (details != null && !details.isEmpty()) {
            handlePsychiatricDetails(details, savedHeader, isNew, user);
        } else if (!isNew) {
            // If no details provided and it's an existing record, delete all details
            deleteAllPsychiatricDetails(savedHeader);
        }

        // 5. Recalculate total score
        BigDecimal totalScore = calculateTotalScore(savedHeader);
        savedHeader.setTotalScore(totalScore);

        return opdPsychiatryAssessmentHeaderRepository.save(savedHeader);
    }

    private void handlePsychiatricDetails(
            List<OpdPsychiatricDetailsRequest> details,
            OpdPsychiatryAssessmentHeader header,
            boolean isNew,
            User user) {

        if (isNew) {
            // For new records, simply save all details
            for (OpdPsychiatricDetailsRequest detailReq : details) {
                OpdPsychiatryAssessmentDetail detail = createPsychiatricDetail(detailReq, header, user);
                opdPsychiatryAssessmentDetailRepository.save(detail);
            }
        } else {
            // For existing records, we need to handle updates more intelligently

            // Get existing details
            List<OpdPsychiatryAssessmentDetail> existingDetails = opdPsychiatryAssessmentDetailRepository
                    .findByAssessmentHeaderId_AssessmentHeaderId(header.getAssessmentHeaderId());

            // Create a map of existing details by question ID for quick lookup
            Map<Long, OpdPsychiatryAssessmentDetail> existingDetailMap = existingDetails.stream()
                    .collect(Collectors.toMap(
                            d -> d.getQuestionId().getId(),
                            d -> d
                    ));

            // Process each incoming detail
            for (OpdPsychiatricDetailsRequest detailReq : details) {
                OpdPsychiatryAssessmentDetail detail = existingDetailMap.get(detailReq.getQuestionId());

                if (detail == null) {
                    // Create new detail if it doesn't exist
                    detail = createPsychiatricDetail(detailReq, header, user);
                    opdPsychiatryAssessmentDetailRepository.save(detail);
                } else {
                    // Update existing detail
                    updatePsychiatricDetail(detail, detailReq, user);
                    opdPsychiatryAssessmentDetailRepository.save(detail);
                    // Remove from map to track which ones are still present
                    existingDetailMap.remove(detailReq.getQuestionId());
                }
            }

            // Delete details that are no longer present in the request
            if (!existingDetailMap.isEmpty()) {
                opdPsychiatryAssessmentDetailRepository.deleteAll(existingDetailMap.values());
                log.info("Deleted {} psychiatric assessment details for header ID: {}",
                        existingDetailMap.size(), header.getAssessmentHeaderId());
            }
        }
    }

    private OpdPsychiatryAssessmentDetail createPsychiatricDetail(
            OpdPsychiatricDetailsRequest detailReq,
            OpdPsychiatryAssessmentHeader header,
            User user) {

        OpdPsychiatryAssessmentDetail detail = new OpdPsychiatryAssessmentDetail();

        detail.setAssessmentHeaderId(header);

        // Set question
        if (detailReq.getQuestionId() != null) {
            detail.setQuestionId(opdQuestionMasterRepository.findById(detailReq.getQuestionId())
                    .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + detailReq.getQuestionId())));
        }

        // Set answer option
        BigDecimal score = BigDecimal.ZERO;
        if (detailReq.getAnswerOptionId() != null) {
            MasQuestionOptionValue optionValue = masQuestionOptionValueRepository
                    .findById(detailReq.getAnswerOptionId())
                    .orElseThrow(() -> new EntityNotFoundException("Option value not found with id: " + detailReq.getAnswerOptionId()));
            detail.setAnswerOptionId(optionValue);
            score = BigDecimal.valueOf(optionValue.getOptionScore());
        }

        detail.setScore(score);
//        detail.setCreatedBy(user.getFullName());
//        detail.setCreatedDate(LocalDateTime.now());

        return detail;
    }

    private void updatePsychiatricDetail(
            OpdPsychiatryAssessmentDetail detail,
            OpdPsychiatricDetailsRequest detailReq,
            User user) {

        // Update question if changed
        if (detailReq.getQuestionId() != null && !detailReq.getQuestionId().equals(detail.getQuestionId().getId())) {
            detail.setQuestionId(opdQuestionMasterRepository.findById(detailReq.getQuestionId())
                    .orElseThrow(() -> new EntityNotFoundException("Question not found with id: " + detailReq.getQuestionId())));
        }

        // Update answer option if changed
        BigDecimal score = BigDecimal.ZERO;
        if (detailReq.getAnswerOptionId() != null) {
            MasQuestionOptionValue optionValue = masQuestionOptionValueRepository
                    .findById(detailReq.getAnswerOptionId())
                    .orElseThrow(() -> new EntityNotFoundException("Option value not found with id: " + detailReq.getAnswerOptionId()));
            detail.setAnswerOptionId(optionValue);
            score = BigDecimal.valueOf(optionValue.getOptionScore());
        } else {
            detail.setAnswerOptionId(null);
        }

        detail.setScore(score);
    }

    private void deleteAllPsychiatricDetails(OpdPsychiatryAssessmentHeader header) {
        List<OpdPsychiatryAssessmentDetail> existingDetails = opdPsychiatryAssessmentDetailRepository
                .findByAssessmentHeaderId_AssessmentHeaderId(header.getAssessmentHeaderId());

        if (!existingDetails.isEmpty()) {
            opdPsychiatryAssessmentDetailRepository.deleteAll(existingDetails);
            log.info("Deleted all psychiatric assessment details for header ID: {}",
                    header.getAssessmentHeaderId());
        }
    }

    private BigDecimal calculateTotalScore(OpdPsychiatryAssessmentHeader header) {
        List<OpdPsychiatryAssessmentDetail> details = opdPsychiatryAssessmentDetailRepository
                .findByAssessmentHeaderId_AssessmentHeaderId(header.getAssessmentHeaderId());

        return details.stream()
                .map(OpdPsychiatryAssessmentDetail::getScore)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void handlePregnancyDetails(
            OpdPatientDetail opd,
            OpdPatientDetailCreateRequest.PregnancyDetails pregnancyDetails,
            User user
    ) {
        if (pregnancyDetails == null) {
            return;
        }

        if (opd.getVisit() == null) {
            throw new IllegalArgumentException(
                    "Visit is required to save pregnancy details"
            );
        }

        if (opd.getPatient() == null) {
            throw new IllegalArgumentException(
                    "Patient is required to save pregnancy details"
            );
        }

        Long visitId = opd.getVisit().getId();
        log.info(
                "Saving pregnancy details for visitId={}, isPregnant={}, lmpDate={}, edd={}, currentEdd={}, gestationPeriod={}",
                visitId,
                pregnancyDetails.getIsPregnant(),
                pregnancyDetails.getLmpDate(),
                pregnancyDetails.getEdd(),
                pregnancyDetails.getCurrentEdd(),
                pregnancyDetails.getGestationPeriod()
        );

        OpdPatientPregnancyDetails pregnancyEntity =
                opdPatientPregnancyDetailsRepository
                        .findByVisit_Id(visitId)
                        .orElseGet(OpdPatientPregnancyDetails::new);

        pregnancyEntity.setVisit(opd.getVisit());
        pregnancyEntity.setPatient(opd.getPatient());
        pregnancyEntity.setIsPregnant(pregnancyDetails.getIsPregnant());
        pregnancyEntity.setLmpDate(pregnancyDetails.getLmpDate());
        pregnancyEntity.setEdd(pregnancyDetails.getEdd());
        pregnancyEntity.setCurrentEdd(pregnancyDetails.getCurrentEdd());
        pregnancyEntity.setGestationPeriod(
                pregnancyDetails.getGestationPeriod()
        );
        pregnancyEntity.setLastChgDate(Instant.now());
        pregnancyEntity.setLastChgBy(
                user != null ? user.getFullName() : null
        );

        OpdPatientPregnancyDetails savedEntity =
                opdPatientPregnancyDetailsRepository.save(pregnancyEntity);

        log.info(
                "Pregnancy details saved successfully. pregnancyDetailsId: {}, OPD patient ID: {}, visit ID: {}",
                savedEntity.getPregnancyDetailsId(),
                opd.getOpdPatientDetailsId(),
                visitId
        );
    }

    @Override
    public ApiResponse<Page<OpdReportListResponse>> getOpdReportsList(Pageable pageable,String mobileNumber, String patientName, Long hospitalId ) {

        log.info("Fetching OPD reports for visitId: {}, page: {}, size: {}",
                pageable.getPageNumber(), pageable.getPageSize());


            Page<OpdReportListProjection> projections =
                    visitRepository.getOpdReportsList(AppConstants.VISIT_STATUS_COMPLETED.toLowerCase(), AppConstants.OPDTYPE, mobileNumber, patientName, pageable);

            Page<OpdReportListResponse> responses = projections.map(projection -> {

                OpdReportListResponse response = new OpdReportListResponse();

                response.setVisitId(projection.getVisitId());
                response.setPatientId(projection.getPatientId());
                response.setPatientName(projection.getPatientName());
                response.setMobileNumber(projection.getMobileNumber());
                response.setUhid(projection.getUhid());
                response.setRelation(projection.getRelation());
                response.setGender(projection.getGender());
                response.setAge(projection.getAge());
                response.setSpecialty(projection.getSpecialty());
                response.setDoctorName(projection.getDoctorName());
                response.setVisitDateTime(projection.getVisitDateTime());
                response.setPrescriptionHdId(projection.getPrescriptionHdId());
                response.setPrescriptionStatus(projection.getPrescriptionStatus());

                return response;
            });

            return ResponseUtils.createSuccessResponse(
                    responses,
                    new TypeReference<>() {}
            );
    }
}

