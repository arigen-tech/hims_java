package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.projection.PrescriptionDetailProjection;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.projection.*;
import com.hims.request.*;
import com.hims.response.*;
import com.hims.service.OpdEntDetailsService;
import com.hims.service.OpdObgDetailsService;
import com.hims.service.OpdOpthDetailsService;
import com.hims.service.OpdPatientDetailService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import com.hims.utils.StockFound;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final RandomNumGenerator randomNumGenerator;

    private final AuthUtil authUtil;

    private final MasStoreItemRepository masStoreItemRepository;

    private final ProcedureDetailsRepository procedureDetailsRepository;

    private final MasCareLevelRepo masCareLevelRepository;

    private final MasWardCategoryRepository masWardCategoryRepository;

    private final MasDepartmentRepository masDepartmentRepository;

    private final BillingHeaderRepository billingHeaderRepository;

    private final BillingDetailRepository billingDetailRepository;

    private final LabOrderTrackingStatusRepository labOrderTrackingStatusRepository;

    private final RadOrderHdRepository radOrderHdRepository;

    private final RadOrderDtRepository radOrderDtRepository;

    private final MasServiceCategoryRepository masServiceCategoryRepository;

    private final PatientPrescriptionHdRepository prescriptionHdRepository;

    private final PatientPrescriptionDtRepository prescriptionDtRepository;

    private final MasSubChargeCodeRepository subChargeCodeRepository;
    private final MasHospitalRepository masHospitalRepository;


    private final OpdOpthDetailsService opdOpthDetailsService;
    private final OpdObgDetailsService opdObgDetailsService;
    private final OpdEntDetailsService opdEntDetailsService;
    private final OpdPatientPregnancyDetailsRepository opdPatientPregnancyDetailsRepository;
    private final OpdPsychiatryAssessmentDetailRepository opdPsychiatryAssessmentDetailRepository;
    private final OpdPsychiatryAssessmentHeaderRepository opdPsychiatryAssessmentHeaderRepository;
    private final MasQuestionHeadingRepository masQuestionHeadingRepository;
    private final OpdQuestionMasterRepository opdQuestionMasterRepository;
    private final MasQuestionOptionValueRepository masQuestionOptionValueRepository;




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
    @Value("${app.opdDepartmentType}")
    private Long departmentTypeOpd;


    public String createOrderNum() {
        return randomNumGenerator.generateOrderNumber("OPD", true, true);
    }

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

//    @Override
//    @Transactional
//    public ApiResponse<OpdPatientDetailResponseDTO> createOpdPatientDetail(OpdPatientDetailCreateRequest request) {
//        // ===================== BASIC VALIDATION =====================
//        if (request == null) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
//            }, "Request body cannot be null", 400);
//        }
//        log.info("Starting createOpdPatientDetail process...");
//        log.info("Request Data: {}", request);
//        Long deptId = authUtil.getCurrentDepartmentId();
//        User useObj = authUtil.getCurrentUser();
//        if (useObj == null || useObj.getHospital() == null) {
//            throw new SDDException("user", 401, "Authenticated user or hospital not found");
//        }
//
//        Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new SDDException("patient", 404, "Patient not found"));
//        Visit visit = visitRepository.findById(request.getVisitId()).orElseThrow(() -> new SDDException("visit", 404, "Visit not found"));
//
//
//        // ===================== CREATE OR UPDATE =====================
//        OpdPatientDetail opdPatientDetail;
//        if (request.getOpdPatientDetailId() == null) {
//            opdPatientDetail = new OpdPatientDetail();
//            log.info("Creating new OpdPatientDetail...");
//        } else {
//            opdPatientDetail = opdPatientDetailRepository.findById(request.getOpdPatientDetailId()).orElseThrow(() -> new SDDException("opdDetail", 404, "OpdPatientDetail not found with ID: " + request.getOpdPatientDetailId()));
//            log.info("Updating OpdPatientDetail ID: {}", request.getOpdPatientDetailId());
//        }
//        // ========================= VITALS =========================
//        opdPatientDetail.setHeight(request.getHeight());
//        opdPatientDetail.setIdealWeight(request.getIdealWeight());
//        opdPatientDetail.setWeight(request.getWeight());
//        opdPatientDetail.setPulse(request.getPulse());
//        opdPatientDetail.setTemperature(request.getTemperature());
//        opdPatientDetail.setRr(request.getRr());
//        opdPatientDetail.setBmi(request.getBmi());
//        opdPatientDetail.setSpo2(request.getSpo2());
//        opdPatientDetail.setBpSystolic(request.getBpSystolic());
//        opdPatientDetail.setBpDiastolic(request.getBpDiastolic());
//        opdPatientDetail.setMlcFlag(request.getMlcFlag());
//        opdPatientDetail.setFinalMedicalAdvice(request.getDoctorRemarks());
//
//        // ========================= DIAGNOSIS =========================
//        if ((request.getWorkingDiagnosis() == null || request.getWorkingDiagnosis().isBlank()) && (request.getIcdDiagnosis() == null || request.getIcdDiagnosis().isEmpty())) {
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
//            }, "One is mandatory: Working Diagnosis or ICD Diagnosis", 400);
//        }
//
//        opdPatientDetail.setWorkingDiag(request.getWorkingDiagnosis());
//
//        if (request.getIcdDiagnosis() != null && !request.getIcdDiagnosis().isEmpty()) {
//            String joinedNames = request.getIcdDiagnosis().stream().filter(Objects::nonNull).map(OpdPatientDetailCreateRequest.IcdDiagnosis::getIcdDiagnosisName).filter(Objects::nonNull).collect(Collectors.joining(","));
//            opdPatientDetail.setIcdDiag(joinedNames);
//        } else {
//            opdPatientDetail.setIcdDiag(null);
//        }
//
//        // ==================== CLINICAL HISTORY =====================
//        opdPatientDetail.setPastMedicalHistory(request.getPastMedicalHistory());
//        opdPatientDetail.setFamilyHistory(request.getFamilyHistory());
//        opdPatientDetail.setClinicalExamination(request.getClinicalExamination());
//        opdPatientDetail.setPatientSignsSymptoms(request.getPatientSignsSymptoms());
//
//        // ====================== INVESTIGATION ======================
//        if (request.getInvestigation() != null && !request.getInvestigation().isEmpty()) {
//            if (request.getInvestigation().stream().anyMatch(i -> i == null || i.getInvestigationDate() == null)) {
//                throw new SDDException("investigation", 400, "Investigation date cannot be null");
//            }
//
//            String orderNumOPD = createOrderNum();
//            LabOrderTrackingStatus labOrderedStatus = labOrderTrackingStatusRepository.findById(orderedStatusId).orElseThrow(() -> new SDDException("status", 500, "Ordered status not found with id: " + orderedStatusId));
//            // Separate investigations by category and date
//            Map<Long, Map<LocalDate, List<OpdPatientDetailCreateRequest.Investigation>>> groupedByCategory = request.getInvestigation().stream().collect(Collectors.groupingBy(
//                    inv -> {
//                        // Get department for each investigation
//                        long departmentId = getDepartmentFromInvestigation(inv.getId());
//                        log.debug("Investigation ID: {} belongs to department: {}",
//                                inv.getId(), departmentId);
//                        return departmentId;
//                    },
//                    Collectors.groupingBy(OpdPatientDetailCreateRequest.Investigation::getInvestigationDate)
//            ));
//
//            log.info("Investigation categories found: {}", groupedByCategory.keySet());
////
//
//        // Process LAB investigations
//            if (laboratoryDepartment != null && groupedByCategory.containsKey(Long.valueOf(laboratoryDepartment))) {
//                log.info("Processing LAB investigations");
//                processLabInvestigations(groupedByCategory.get(Long.valueOf(laboratoryDepartment)), patient, visit, useObj, deptId, orderNumOPD, labOrderedStatus);
//                opdPatientDetail.setLabFlag(AppConstants.STATUS_Y.toLowerCase());
//
//            } else {
//                log.warn("Laboratory department {} not found in investigation categories. Available: {}",
//                        laboratoryDepartment, groupedByCategory.keySet());
//            }
//
//        // Process RADIOLOGY investigations
//            if (radiologyDepartment != null && groupedByCategory.containsKey(Long.valueOf(radiologyDepartment))) {
//                log.info("Processing RADIOLOGY investigations");
//                processRadiologyInvestigations(groupedByCategory.get(Long.valueOf(radiologyDepartment)), patient, visit, useObj);
//                opdPatientDetail.setRadioFlag(request.getRadioFlag());
//
//            } else {
//                log.warn("Radiology department {} not found in investigation categories. Available: {}",
//                        radiologyDepartment, groupedByCategory.keySet());
//            }
//        }
//
//        // ======================== TREATMENT ========================
//        opdPatientDetail.setTreatmentAdvice(request.getTreatmentAdvice());
//        if (request.getTreatment() != null && !request.getTreatment().isEmpty()) {
//
//            PatientPrescriptionHd hd = new PatientPrescriptionHd();
//            hd.setHospitalId(useObj.getHospital().getId());
//            hd.setPatientId(patient.getId());
//            hd.setDepartmentId(deptId);
//            hd.setDoctorName(useObj.getFirstName());
//            hd.setPrescriptionDate(LocalDateTime.now());
//            hd.setStatus(AppConstants.STATUS_N.toLowerCase());
//            hd.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase().equalsIgnoreCase(useObj.getHospital().getMedicineBilling()) ? "n" : "y");
//            hd.setCreatedBy(useObj.getFirstName());
//            hd.setTotalCost(BigDecimal.ZERO);
//            hd.setTotalGst(BigDecimal.ZERO);
//            hd.setTotalDiscount(BigDecimal.ZERO);
//            hd.setNetAmount(BigDecimal.ZERO);
//            hd.setVisit(visit);
//
//            PatientPrescriptionHd savedHd = patientPrescriptionHdRepository.save(hd);
//
//            for (OpdPatientDetailCreateRequest.Treatment trt : request.getTreatment()) {
//                if (trt == null) continue;
//
//                PatientPrescriptionDt dt = new PatientPrescriptionDt();
//                dt.setPrescriptionHdId(savedHd.getPrescriptionHdId());
//                dt.setItemId(trt.getItemId());
//                dt.setDosage(trt.getDosage());
//                dt.setFrequency(trt.getFrequency());
//                dt.setDays(trt.getDays());
//                dt.setTotal(trt.getTotal());
//                dt.setInstruction(trt.getInstraction());
//                dt.setStatus(AppConstants.STATUS_N.toLowerCase());
//
//                patientPrescriptionDtRepository.save(dt);
//            }
//        }
//
//        // ====================== GENERAL DETAILS =====================
//        opdPatientDetail.setOpdDate(Instant.now());
//        opdPatientDetail.setPatient(patient);
//        opdPatientDetail.setVisit(visit);
//        opdPatientDetail.setDepartment(departmentRepository.findById(deptId).orElseThrow(() -> new SDDException("department", 404, "Department not found")));
//        opdPatientDetail.setHospital(hospitalRepository.findById(useObj.getHospital().getId()).orElseThrow(() -> new SDDException("hospital", 404, "Hospital not found")));
//        opdPatientDetail.setDoctor(userRepository.findById(useObj.getUserId()).orElseThrow(() -> new SDDException("doctor", 404, "Doctor not found")));
//
//        opdPatientDetail.setLastChgBy(useObj.getUsername());
//        opdPatientDetail.setLastChgDate(Instant.now());
//
//        // ========================= Admission Advice =====================================
//
//        if (isYes(request.getAdmissionFlag())) {
//            masCareLevelRepository.findById(request.getAdmissionCareLevel()).ifPresent(opdPatientDetail::setAdmissionCareLevel);
//            masWardCategoryRepository.findById(request.getAdmissionWardCategory()).ifPresent(opdPatientDetail::setAdmissionWardCategory);
//            masDepartmentRepository.findById(request.getAdmissionWard()).ifPresent(opdPatientDetail::setAdmissionWard);
//            opdPatientDetail.setAdmissionFlag(AppConstants.STATUS_Y.toLowerCase());
//            opdPatientDetail.setAdmissionAdvisedDate(request.getAdmissionAdvisedDate());
//            opdPatientDetail.setAdmissionRemarks(request.getAdmissionRemarks());
//            opdPatientDetail.setAdmissionPriority(request.getAdmissionPriority());
//        } else {
//            opdPatientDetail.setAdmissionFlag(AppConstants.STATUS_N.toLowerCase());
//        }
//
//        // ========================= Follow up =========================
//        if (isYes(request.getFollowUpFlag())) {
//            opdPatientDetail.setFollowUpFlag(AppConstants.STATUS_Y.toLowerCase());
//            opdPatientDetail.setFollowUpDays(request.getFollowUpDays());
//            opdPatientDetail.setFollowUpDate(request.getFollowUpDate());
//        } else {
//            opdPatientDetail.setFollowUpFlag(AppConstants.STATUS_N.toLowerCase());
//        }
//
//        //  =========================== referral ==============================
//        opdPatientDetail.setReferralFlag(isYes(request.getReferralFlag()) ? AppConstants.STATUS_Y.toLowerCase() : AppConstants.STATUS_N.toLowerCase());
//        opdPatientDetail.setReferralRemarks(request.getReferralRemarks());
//        opdPatientDetail.setReferredHospitalName(request.getReferredHospitalName());
//        opdPatientDetail.setReferralDate(request.getReferralDate());
//        opdPatientDetail.setReferTo(request.getReferTo());
//
//
//        // ====================== SAVE OPD ============================
//        OpdPatientDetail saved = opdPatientDetailRepository.save(opdPatientDetail);
//
//        // ====================== ICD SAVE (NO DUPLICATES) ============
//        if (request.getIcdDiagnosis() != null && !request.getIcdDiagnosis().isEmpty()) {
//
//            dischargeIcdCodeRepository.deleteByOpdPatientDetailsId(saved.getOpdPatientDetailsId());
//            for (OpdPatientDetailCreateRequest.IcdDiagnosis icd : request.getIcdDiagnosis()) {
//                if (icd == null) continue;
//                DischargeIcdCode code = new DischargeIcdCode();
//                code.setIcdId(icd.getIcdId());
//                code.setOpdPatientDetailsId(saved.getOpdPatientDetailsId());
//                code.setVisitId(request.getVisitId());
//                code.setAddEditById(useObj.getUserId());
//                code.setAddEditDate(LocalDate.now());
//                code.setAddEditTime(LocalTime.now().toString());
//
//                dischargeIcdCodeRepository.save(code);
//            }
//        }
//
//        // ================================ Procedure Care =====================

    /// /        if (request.getProcedureCare() != null && !request.getProcedureCare().isEmpty()) {
    /// /
    /// /            log.info("Creating Procedure Header & Procedure Details...");
    /// /
    /// /            Patient patObj = patientRepository.findById(request.getPatientId())
    /// /                    .orElseThrow(() -> new RuntimeException("Patient not found"));
    /// /
    /// /            Visit visitObj = visitRepository.findById(request.getVisitId())
    /// /                    .orElseThrow(() -> new RuntimeException("Visit not found"));
    /// /
    /// /            MasHospital hosObj = useObj.getHospital();
    /// /
    /// /            // *************** CREATE HEADER ***************
    /// /            ProcedureHeader header = new ProcedureHeader();
    /// /            header.setStatus("n");
    /// /            header.setLastChangedDate(LocalDate.now());
    /// /            header.setLastChangedTime(LocalTime.now().toString());
    /// /            header.setRequisitionDate(LocalDate.now());
    /// /            header.setProcedureDate(LocalDateTime.now());
    /// /            header.setProcedureTime(LocalTime.now().toString());
    /// /            header.setHinId(Math.toIntExact(patObj.getId()));
    /// /            header.setHospital(hosObj);
    /// /            header.setLastChangedBy(Math.toIntExact(useObj.getUserId()));
    /// /            header.setMedicalOfficerId(Math.toIntExact(useObj.getUserId()));
    /// /            header.setVisitId(Math.toIntExact(visitObj.getId()));
    /// /            header.setOpdPatientDetailsId(Math.toIntExact(saved.getOpdPatientDetailsId()));
    /// /            header.setProcedureType("OPD"); // OPD procedure
    /// /
    /// /            ProcedureHeader savedHeader = procedureHeaderRepository.save(header);
    /// /
    /// /
    /// /            // *************** CREATE MULTIPLE DETAILS ***************
    /// /            for (OpdPatientDetailCreateRequest.ProcedureCare req : request.getProcedureCare()) {
    /// /
    /// /                MasProcedure procEntity = masProcedureRepository.findById(req.getProcedureId())
    /// /                        .orElseThrow(() ->
    /// /                                new RuntimeException("Procedure not found with ID: " + req.getProcedureId()));
    /// /
    /// /                ProcedureDetails details = ProcedureDetails.builder()
    /// /                        .procedureHeader(savedHeader)
    /// /                        .remarks(req.getRemarks())
    /// /                        .procedureName(req.getProcedureName())
    /// /                        .status("n")
    /// /                        .masProcedure(procEntity)
    /// /                        .frequencyId(req.getFrequencyId() != null ? req.getFrequencyId().intValue() : null)
    /// /                        .noOfDays(req.getNoOfDays() != null ? req.getNoOfDays().intValue() : null)
    /// /                        .appointmentDate(LocalDate.now())
    /// /                        .finalProcedureStatus("n")
    /// /                        .nursingRemark(null)
    /// /                        .nextAppointmentDate(null)
    /// /                        .appointmentTime(LocalTime.now().toString())
    /// /                        .procedureDate(LocalDate.now())
    /// /                        .procedureTime(LocalTime.now().toString())
    /// /                        .build();
    /// /
    /// /                procedureDetailsRepository.save(details);
    /// /            }
    /// /        }
    /// /
//        // ====================== VISIT CLOSE =========================
//        visit.setVisitStatus(AppConstants.VISIT_STATUS_COMPLETED.toLowerCase());
//        visitRepository.save(visit);
//
//        return ResponseUtils.createSuccessResponse(null, new TypeReference<>() {
//        });
//    }
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
        OpdPatientDetail opd = opdPatientDetailRepository.findByVisit_Id(request.getVisitId());
        // If OPD patient detail doesn't exist, create a new one
        if (opd == null) {
            opd = new OpdPatientDetail();
            log.info("Creating new OPD Patient Detail for visit ID: {}", request.getVisitId());
        }
        if ((request.getWorkingDiagnosis() == null || request.getWorkingDiagnosis().isBlank()) && (request.getIcdDiagnosis() == null || request.getIcdDiagnosis().isEmpty())) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "One is mandatory: Working Diagnosis or ICD Diagnosis", 400);
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
        saveIcdDiagnosis(request.getIcdDiagnosis(), saved.getOpdPatientDetailsId(), request.getVisitId(), user.getUserId());
        boolean hasLabInvestigations = false;
        boolean hasRadioInvestigations = false;
        if (request.getInvestigation() != null && !request.getInvestigation().isEmpty()) {
            if (request.getInvestigation().stream().anyMatch(i -> i == null || i.getInvestigationDate() == null)) {
                throw new SDDException("investigation", 400, "Investigation date cannot be null");
            }
            String orderNum = createOrderNum();
            LabOrderTrackingStatus labOrderedStatus = labOrderTrackingStatusRepository.findById(orderedStatusId).orElseThrow(() -> new SDDException("status", 500, "Ordered status not found with id: " + orderedStatusId));

            // Group investigations by department
            Map<Long, Map<LocalDate, List<OpdPatientDetailCreateRequest.Investigation>>> grouped = request.getInvestigation().stream().filter(Objects::nonNull).collect(Collectors.groupingBy(inv -> getDepartmentFromInvestigation(inv.getId()), Collectors.groupingBy(OpdPatientDetailCreateRequest.Investigation::getInvestigationDate)));

            if (grouped.containsKey(Long.valueOf(laboratoryDepartment))) {
                log.info("Processing LAB investigations");
                processLabInvestigations(grouped.get(Long.valueOf(laboratoryDepartment)), patient, visit, user, deptId, orderNum, labOrderedStatus);
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
            saveTreatments(request.getTreatment(), patient, visit, user, deptId);
        }

        if (request.getOphthalmologyExaminationDetails() != null) {
            OpdOpthDetailsRequest opthRequest = request.getOphthalmologyExaminationDetails();
            opthRequest.setPatientId(patient.getId());
            opthRequest.setVisitId(visit.getId());
            ApiResponse<String> response = opdOpthDetailsService.opdVisionExaminationDetailsSave(opthRequest);
            if (response == null|| response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("ophthalmology",500,response != null? response.getMessage(): "Failed to save ophthalmology details");
            }
        }
        if(request.getOpdObgDetailsRequest() != null){
            request.getOpdObgDetailsRequest().setPatientId(patient.getId());
            request.getOpdObgDetailsRequest().setVisitId(visit.getId());
            ApiResponse<String> response = opdObgDetailsService.createOrUpdateObgDetails(request.getVisitId(),request.getOpdObgDetailsRequest());
            if (response == null|| response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("obg",500,response != null? response.getMessage(): "Failed to save OBG details");
            }
        }
        if(request.getEntExaminationDetails()!= null){
            ApiResponse<String> response = opdEntDetailsService.createOrUpdateEntDetails(request.getVisitId(),request.getEntExaminationDetails());
            if (response == null|| response.getStatus() != HttpStatus.OK.value()) {
                throw new SDDException("ent",500,response != null? response.getMessage(): "Failed to save ENT details");
            }
        }
        if(request.getPregnancyDetails() != null) {
            handlePregnancyDetails(saved, request.getPregnancyDetails(), user);
        }
        // ================= Psychiatric Assessment Save =================
        if(request.getDetails() != null && !request.getDetails().isEmpty()) {
            log.info("Saving Psychiatric Assessment Header and Details for OPD ID: {}", saved.getOpdPatientDetailsId());
            savePsychiatricHeaderAndDetails(request, visit, saved);
        }
        opdPatientDetailRepository.save(saved);
        closeVisit(visit);
        log.info("Successfully completed OPD patient detail creation for visit ID: {}", visit.getId());
        return ResponseUtils.createSuccessResponse(null, new TypeReference<>() {
        });
    }

    private Long getDepartmentFromInvestigation(Long investigationId) {
        if (investigationId == null) {
            throw new SDDException("investigation", 400, "Investigation ID cannot be null");
        }

        DgMasInvestigation investigation = dgMasInvestigationRepository.findById(investigationId).orElseThrow(() -> new SDDException("investigation", 404, "Investigation not found with ID: " + investigationId));

        if (investigation.getSubChargeCodeId() == null) {
            throw new SDDException("subcharge", 400, "Subcharge code not configured for investigation ID: " + investigationId);
        }

        Long subChargeId = investigation.getSubChargeCodeId().getSubId();
        if (subChargeId == null) {
            throw new SDDException("subcharge", 400, "SubCharge ID is null for investigation ID: " + investigationId);
        }

        MasSubChargeCode subChargeCode = subChargeCodeRepository.findById(subChargeId).orElseThrow(() -> new SDDException("subcharge", 404, "Subcharge code not found for investigation ID: " + investigationId));

        if (subChargeCode.getMasDepartment() == null) {
            throw new SDDException("department", 400, "Department not configured for subcharge code");
        }

        return subChargeCode.getMasDepartment().getId();
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
            visitRepository.save(visit);
            log.info("Closed visit with ID: {}", visit.getId());
        }
    }


//    @Transactional(rollbackFor = Exception.class)
//    @Override
//    public ApiResponse<String> recallOpdPatientDetail(RecallOpdPatientDetailRequest request) {
//        log.info("==== START recallOpdPatientDetail ====");
//        try {
//
//            // ===================== BASIC VALIDATION =====================
//
//            if (request == null) {
//                throw new IllegalArgumentException("Request cannot be null");
//            }
//
//            Objects.requireNonNull(request.getPatientId(), "OPD Patient ID is required");
//            Objects.requireNonNull(request.getPatientId(), "Patient ID is required");
//            Objects.requireNonNull(request.getVisitId(), "Visit ID is required");
//            Objects.requireNonNull(request.getDepartmentId(), "Department ID is required");
//            Objects.requireNonNull(request.getHospitalId(), "Hospital ID is required");
//
//            User useObj = authUtil.getCurrentUser();
//            if (useObj == null || useObj.getHospital() == null) {
//                throw new RuntimeException("Authenticated user or hospital not found");
//            }
//
//            // ===================== FETCH OPD =====================
//
//            OpdPatientDetail opdObj = opdPatientDetailRepository.findById(request.getPatientId()).orElseThrow(() -> new RuntimeException("OPD record not found: " + request.getPatientId()));
//
//            log.info("Fetched OPD record successfully");
//
//            // ===================== UPDATE VITALS =====================
//
//            opdObj.setHeight(request.getHeight());
//            opdObj.setWeight(request.getWeight());
//            opdObj.setTemperature(request.getTemperature());
//            opdObj.setBpDiastolic(request.getDiastolicBP());
//            opdObj.setBpSystolic(request.getSystolicBP());
//            opdObj.setPulse(request.getPulse());
//            opdObj.setBmi(request.getBmi());
//            opdObj.setRr(request.getRr());
//            opdObj.setSpo2(request.getSpo2());
//            opdObj.setPatientSignsSymptoms(request.getPatientSymptoms());
//            opdObj.setClinicalExamination(request.getClinicalExamination());
//            opdObj.setPastMedicalHistory(request.getPastHistory());
//            opdObj.setFamilyHistory(request.getFamilyHistory());
//            opdObj.setMlcFlag(request.getMlcCase());
//            opdObj.setWorkingDiag(request.getWorkingDiagnosis());
//            opdObj.setIcdDiag(request.getIcdDiagnosis());
//            opdObj.setFinalMedicalAdvice(request.getDoctorRemarks());
//
//            OpdPatientDetail saved = opdPatientDetailRepository.save(opdObj);
//
//            // ===================== ICD =====================
//
//            if (request.getIcdObj() != null) {
//
//                for (RecallOpdPatientDetailRequest.IcdDiagnosis icd : request.getIcdObj()) {
//
//                    if (icd == null) continue;
//
//                    if (icd.getId() == null) {
//                        DischargeIcdCode newIcd = new DischargeIcdCode();
//                        newIcd.setIcdId(icd.getIcdId());
//                        newIcd.setOpdPatientDetailsId(saved.getOpdPatientDetailsId());
//                        newIcd.setVisitId(request.getVisitId());
//                        newIcd.setAddEditById(useObj.getUserId());
//                        newIcd.setAddEditDate(LocalDate.now());
//                        newIcd.setAddEditTime(LocalTime.now().toString());
//                        dischargeIcdCodeRepository.save(newIcd);
//                    } else {
//                        DischargeIcdCode existing = dischargeIcdCodeRepository.findById(icd.getId()).orElseThrow(() -> new RuntimeException("ICD not found"));
//                        existing.setIcdId(icd.getIcdId());
//                        existing.setAddEditById(useObj.getUserId());
//                        existing.setAddEditDate(LocalDate.now());
//                        existing.setAddEditTime(LocalTime.now().toString());
//                        dischargeIcdCodeRepository.save(existing);
//                    }
//                }
//            }
//
//            // ===================== INVESTIGATIONS =====================
//
//            opdObj.setLabFlag(request.getLabFlag());
//
//            List<RecallOpdPatientDetailRequest.InvestigationRequest> invList = request.getInvestigations();
//
//            if (invList != null && !invList.isEmpty()) {
//
//                log.info("Processing investigations");
//
//                String orderNumOPD = createOrderNum();
//
//                Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));
//
//                Visit visit = visitRepository.findById(request.getVisitId()).orElseThrow(() -> new RuntimeException("Visit not found"));
//
//                Map<LocalDate, DgOrderHd> existingHdByDate = new HashMap<>();
//
//                List<DgOrderHd> existingHdList = dgOrderHdRepo.findAllByVisitId(visit);
//                if (existingHdList != null) {
//                    for (DgOrderHd hd : existingHdList) {
//                        if (hd != null && hd.getAppointmentDate() != null) {
//                            existingHdByDate.put(hd.getAppointmentDate(), hd);
//                        }
//                    }
//                }
//
//                Map<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> grouped = invList.stream().filter(Objects::nonNull).filter(i -> i.getId() == null).filter(i -> i.getDate() != null).collect(Collectors.groupingBy(RecallOpdPatientDetailRequest.InvestigationRequest::getDate));
//
//                for (Map.Entry<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> entry : grouped.entrySet()) {
//
//                    DgOrderHd dgOrderHd = existingHdByDate.computeIfAbsent(entry.getKey(), date -> {
//                        DgOrderHd hd = new DgOrderHd();
//                        hd.setOrderDate(LocalDate.now());
//                        hd.setAppointmentDate(date);
//                        hd.setOrderNo(orderNumOPD);
//                        hd.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
//                        hd.setCollectionStatus(AppConstants.STATUS_N.toLowerCase());
//                        hd.setPaymentStatus(AppConstants.STATUS_N.toLowerCase());
//                        hd.setSource(AppConstants.OPD_PATIENT);
//                        hd.setDiscountId(1);
//                        hd.setPatientId(patient);
//                        hd.setDepartmentId(request.getDepartmentId());
//                        hd.setHospitalId(request.getHospitalId());
//                        hd.setVisitId(visit);
//                        hd.setCreatedBy(useObj.getFirstName());
//                        hd.setLastChgBy(useObj.getFirstName());
//                        hd.setCreatedOn(LocalDate.now());
//                        hd.setLastChgDate(LocalDate.now());
//                        hd.setLastChgTime(LocalTime.now().toString());
//                        return dgOrderHdRepo.save(hd);
//                    });
//
//                    for (RecallOpdPatientDetailRequest.InvestigationRequest inv : entry.getValue()) {
//
//                        if (inv.getInvestigationId() == null) continue;
//
//                        DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(inv.getInvestigationId()).orElseThrow(() -> new RuntimeException("Investigation not found: " + inv.getInvestigationId()));
//
//                        DgOrderDt dt = new DgOrderDt();
//                        dt.setOrderhdId(dgOrderHd);
//                        dt.setInvestigationId(invEntity);
//                        dt.setAppointmentDate(inv.getDate());
//                        dt.setOrderQty(1);
//                        dt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
//                        dt.setBillingStatus(AppConstants.STATUS_N.toLowerCase());
//                        dt.setCreatedBy(useObj.getFirstName());
//                        dt.setLastChgBy(useObj.getFirstName());
//                        dt.setCreatedon(Instant.now());
//                        dt.setLastChgDate(LocalDate.now());
//                        dt.setLastChgTime(LocalTime.now().toString());
//
//                        if (invEntity.getMainChargeCodeId() != null) {
//                            dt.setMainChargecodeId(invEntity.getMainChargeCodeId().getChargecodeId());
//                        }
//                        if (invEntity.getSubChargeCodeId() != null) {
//                            dt.setSubChargeid(invEntity.getSubChargeCodeId().getSubId());
//                        }
//
//                        dgOrderDtRepo.save(dt);
//                    }
//                }
//            }
//            // ===================== RADIOLOGY =====================
//
//            opdObj.setRadioFlag(request.getRadioFlag());
//
//            List<RecallOpdPatientDetailRequest.InvestigationRequest> radioList = request.getInvestigations();
//
//            if (radioList != null && !radioList.isEmpty()) {
//
//                log.info("Processing radiology investigations");
//
//                String orderNumRAD = createOrderNum();
//
//                Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));
//
//                Visit visit = visitRepository.findById(request.getVisitId()).orElseThrow(() -> new RuntimeException("Visit not found"));
//
//                Map<LocalDate, RadOrderHd> existingRadHdByDate = new HashMap<>();
//
//                List<RadOrderHd> existingRadHdList =
//                        radOrderHdRepository.findAllByVisitId(visit.getId());
//
//                if (existingRadHdList != null) {
//                    for (RadOrderHd hd : existingRadHdList) {
//
//                        if (hd != null && hd.getAppointmentDate() != null) {
//                            existingRadHdByDate.put(hd.getAppointmentDate(), hd);
//                        }
//                    }
//                }
//
//                Map<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> grouped =
//                        radioList.stream()
//                                .filter(Objects::nonNull)
//                                .filter(i -> i.getId() == null)
//                                .filter(i -> i.getDate() != null)
//                                .collect(Collectors.groupingBy(
//                                        RecallOpdPatientDetailRequest.InvestigationRequest::getDate
//                                ));
//
//                for (Map.Entry<LocalDate,
//                        List<RecallOpdPatientDetailRequest.InvestigationRequest>> entry
//                        : grouped.entrySet()) {
//
//                    RadOrderHd radOrderHd = existingRadHdByDate.computeIfAbsent(entry.getKey(),
//                            date -> {
//
//                                RadOrderHd hd = new RadOrderHd();
//                                hd.setOrderDate(LocalDate.now());
//                                hd.setAppointmentDate(date);
//                                hd.setOrderTime(Instant.now());
//                                hd.setPatient(patient);hd.setVisit(visit);
//
//                                hd.setHospital(masHospitalRepository.findById(request.getHospitalId())
//                                        .orElseThrow(() ->
//                                                        new RuntimeException("Hospital not found")));
//
//                                hd.setDepartment(masDepartmentRepository.findById(request.getDepartmentId()).orElseThrow(() ->
//                                                        new RuntimeException("Department not found")));
//
//                                hd.setPrescribedBy(useObj.getFirstName());
//                                hd.setPaymentStatus(AppConstants.STATUS_N.toLowerCase());
//                                hd.setCreatedby(useObj.getFirstName());
//                                hd.setCreatedon(Instant.now());
//                                hd.setLastChgBy(useObj.getFirstName());
//                                hd.setLastChgDate(Instant.now());
//                                return radOrderHdRepository.save(hd);
//                            });
//
//                    for (RecallOpdPatientDetailRequest.InvestigationRequest inv
//                            : entry.getValue()) {
//
//                        if (inv.getInvestigationId() == null) {
//                            continue;
//                        }
//
//                        DgMasInvestigation invEntity = dgMasInvestigationRepository
//                                .findById(inv.getInvestigationId()).orElseThrow(() -> new RuntimeException("Radiology Investigation not found : " + inv.getInvestigationId()));
//
//                        RadOrderDt dt = new RadOrderDt();
//                        dt.setRadOrderhd(radOrderHd);
//                        dt.setInvestigation(invEntity);
//                        if (invEntity.getSubChargeCodeId() != null) {
//                            dt.setSubChargecode(invEntity.getSubChargeCodeId());
//                        }
//                        dt.setOrderAccessionNo(orderNumRAD);
//                        dt.setAppointmentDate(inv.getDate());
//                        dt.setStudyStatus(AppConstants.STATUS_N.toLowerCase());
//                        dt.setReportStatus(AppConstants.STATUS_N.toLowerCase());
//                        dt.setHl7MwlStatus(AppConstants.STATUS_N.toLowerCase());
//                        dt.setPacsCompletionStatus(AppConstants.STATUS_N.toLowerCase());
//                        dt.setBillingStatus(AppConstants.STATUS_N.toLowerCase());
//                        dt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
//                        dt.setCreatedby(useObj.getFirstName());dt.setCreatedon(Instant.now());
//                        dt.setLastChgBy(useObj.getFirstName());
//                        dt.setLastChgDate(Instant.now());
//                        radOrderDtRepository.save(dt);
//                    }
//                }
//            }
//
//            // ===================== TREATMENT =====================
//
//            opdObj.setTreatmentAdvice(request.getTreatmentAdvice());
//            List<RecallOpdPatientDetailRequest.TreatmentRequest> treatments = request.getTreatments();
//
//            if (treatments != null && !treatments.isEmpty()) {
//
//                Long prescriptionHdId;
//
//                Optional<RecallOpdPatientDetailRequest.TreatmentRequest> existing = treatments.stream().filter(t -> t != null && t.getTreatmentId() != null).findFirst();
//
//                if (existing.isPresent()) {
//                    PatientPrescriptionDt dt = patientPrescriptionDtRepository.findById(existing.get().getTreatmentId()).orElseThrow(() -> new RuntimeException("Treatment not found"));
//                    prescriptionHdId = dt.getPrescriptionHdId();
//                } else {
//
//                    patientPrescriptionHdRepository.findLatestByPatientId(request.getPatientId()).ifPresent(hd -> {
//                        patientPrescriptionDtRepository.deleteByPrescriptionHdId(hd.getPrescriptionHdId());
//                        patientPrescriptionHdRepository.deleteById(hd.getPrescriptionHdId());
//                    });
//
//                    Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));
//
//                    PatientPrescriptionHd hd = new PatientPrescriptionHd();
//                    hd.setHospitalId(useObj.getHospital().getId());
//                    hd.setPatientId(patient.getId());
//                    hd.setDepartmentId(request.getDepartmentId());
//                    hd.setDoctorName(useObj.getFirstName());
//                    hd.setPrescriptionDate(LocalDateTime.now());
//                    hd.setStatus(AppConstants.STATUS_N.toLowerCase());
//                    hd.setCreatedBy(useObj.getFirstName());
//                    hd.setTotalCost(BigDecimal.ZERO);
//                    hd.setTotalGst(BigDecimal.ZERO);
//                    hd.setTotalDiscount(BigDecimal.ZERO);
//                    hd.setNetAmount(BigDecimal.ZERO);
////                    hd.setVisit(visit);
//
//                    prescriptionHdId = patientPrescriptionHdRepository.save(hd).getPrescriptionHdId();
//                }
//
//                Long finalHdId = prescriptionHdId;
//
//                treatments.stream().filter(t -> t != null && t.getTreatmentId() == null).forEach(trt -> {
//                    PatientPrescriptionDt dt = new PatientPrescriptionDt();
//                    dt.setPrescriptionHdId(finalHdId);
//                    dt.setItemId(trt.getDrugId());
//                    dt.setDosage(trt.getDosage());
//                    dt.setFrequency(trt.getFrequency());
//                    dt.setDays(trt.getDays());
//                    dt.setTotal(trt.getTotal() == null ? BigDecimal.ZERO : BigDecimal.valueOf(trt.getTotal()));
//                    dt.setInstruction(trt.getInstruction());
//                    dt.setStatus(AppConstants.STATUS_N.toLowerCase());
//                    patientPrescriptionDtRepository.save(dt);
//                });
//            }
//
//            // ===================== ADMISSION =====================
//
//            if (isYes(request.getAdmissionFlag())) {
//
//                opdObj.setAdmissionFlag(AppConstants.STATUS_Y.toLowerCase());
//
//                if (request.getAdmissionCareLevel() != null) {
//                    masCareLevelRepository.findById(request.getAdmissionCareLevel()).ifPresent(opdObj::setAdmissionCareLevel);
//                }
//
//                if (request.getAdmissionWardCategory() != null) {
//                    masWardCategoryRepository.findById(request.getAdmissionWardCategory()).ifPresent(opdObj::setAdmissionWardCategory);
//                }
//
//                if (request.getAdmissionWard() != null) {
//                    masDepartmentRepository.findById(request.getAdmissionWard()).ifPresent(opdObj::setAdmissionWard);
//                }
//
//                opdObj.setAdmissionAdvisedDate(request.getAdmissionAdvisedDate());
//                opdObj.setAdmissionRemarks(request.getAdmissionRemarks());
//                opdObj.setAdmissionPriority(request.getAdmissionPriority());
//
//            } else {
//                opdObj.setAdmissionFlag(AppConstants.STATUS_N.toLowerCase());
//
//                opdObj.setAdmissionCareLevel(null);
//                opdObj.setAdmissionWardCategory(null);
//                opdObj.setAdmissionWard(null);
//                opdObj.setAdmissionAdvisedDate(null);
//                opdObj.setAdmissionRemarks(null);
//                opdObj.setAdmissionPriority(null);
//            }
//
//
//            // ===================== FOLLOW UP =====================
//
//            if (isYes(request.getFollowUpFlag())) {
//
//                opdObj.setFollowUpFlag(AppConstants.STATUS_Y.toLowerCase());
//                opdObj.setFollowUpDays(request.getFollowUpDays());
//                opdObj.setFollowUpDate(request.getFollowUpDate());
//
//            } else {
//                opdObj.setFollowUpFlag(AppConstants.STATUS_N.toLowerCase());
//                opdObj.setFollowUpDays(null);
//                opdObj.setFollowUpDate(null);
//            }
//
//            // ===================== Referral =====================
//
//            if (isYes(request.getReferralFlag())) {
//
//                opdObj.setReferralFlag(AppConstants.STATUS_Y.toLowerCase());
//                opdObj.setReferralRemarks(request.getReferralRemarks());
//                opdObj.setReferralDate(request.getReferralDate());
//
//            } else {
//                opdObj.setReferralFlag(AppConstants.STATUS_N.toLowerCase());
//                opdObj.setReferralRemarks(null);
//                opdObj.setReferralDate(null);
//            }
//
//            deleteDischargeIcd(request.getRemoveIcdIds());
//            deleteOrderDetails(request.getRemovedInvestigationIds());
//            deletePrescriptionDetails(request.getRemovedTreatmentIds());
//
//            // ================================ Procedure Care =====================
//

    /// /        List<RecallOpdPatientDetailRequest.ProcedureCare> careList = request.getProcedureCare();
    /// /
    /// /        boolean allIdsNull = careList.stream()
    /// /                .allMatch(c -> c.getId() == null);
    /// /
    /// /        ProcedureHeader existingHeader =
    /// /                procedureHeaderRepository.findByVisitId(request.getVisitId())
    /// /                        .orElse(null);
    /// /        if (allIdsNull && existingHeader != null) {
    /// /
    /// /            procedureDetailsRepository.deleteByProcedureHeader(existingHeader);
    /// /
    /// /            procedureHeaderRepository.delete(existingHeader);
    /// /
    /// /            existingHeader = null;
    /// /        }
    /// /
    /// /        ProcedureHeader header = existingHeader;
    /// /
    /// /        if (header == null) {
    /// /            header = new ProcedureHeader();
    /// /            header.setStatus("n");
    /// /            header.setLastChangedDate(LocalDate.now());
    /// /            header.setLastChangedTime(LocalTime.now().toString());
    /// /            header.setRequisitionDate(LocalDate.now());
    /// /            header.setProcedureDate(LocalDateTime.now());
    /// /            header.setProcedureTime(LocalTime.now().toString());
    /// /            header.setHinId(Math.toIntExact(request.getPatientId()));
    /// /            header.setHospital(useObj.getHospital());
    /// /            header.setLastChangedBy(Math.toIntExact(useObj.getUserId()));
    /// /            header.setMedicalOfficerId(Math.toIntExact(useObj.getUserId()));
    /// /            header.setVisitId(Math.toIntExact(request.getVisitId()));
    /// /            header.setOpdPatientDetailsId(Math.toIntExact(saved.getOpdPatientDetailsId()));
    /// /            header.setProcedureType("OPD");
    /// /
    /// /            header = procedureHeaderRepository.save(header);
    /// /        }
    /// /
    /// /        for (RecallOpdPatientDetailRequest.ProcedureCare req : careList) {
    /// /
    /// /            MasProcedure procEntity = masProcedureRepository.findById(req.getProcedureId())
    /// /                    .orElseThrow(() ->
    /// /                            new RuntimeException("Procedure not found with ID: " + req.getProcedureId()));
    /// /
    /// /            if (req.getId() == null) {
    /// /                // ===== CREATE =====
    /// /                ProcedureDetails details = new ProcedureDetails();
    /// /                details.setProcedureHeader(header);
    /// /                details.setMasProcedure(procEntity);
    /// /                details.setProcedureName(req.getProcedureName());
    /// /                details.setRemarks(req.getRemarks());
    /// /                details.setFrequencyId(req.getFrequencyId() != null ? req.getFrequencyId().intValue() : null);
    /// /                details.setNoOfDays(req.getNoOfDays() != null ? req.getNoOfDays().intValue() : null);
    /// /                details.setStatus("n");
    /// /                details.setFinalProcedureStatus("n");
    /// /                details.setAppointmentDate(LocalDate.now());
    /// /                details.setAppointmentTime(LocalTime.now().toString());
    /// /                details.setProcedureDate(LocalDate.now());
    /// /                details.setProcedureTime(LocalTime.now().toString());
    /// /
    /// /                procedureDetailsRepository.save(details);
    /// /
    /// /            } else {
    /// /                // ===== UPDATE =====
    /// /                ProcedureDetails details = procedureDetailsRepository.findById(req.getId())
    /// /                        .orElseThrow(() ->
    /// /                                new RuntimeException("Procedure detail not found: " + req.getId()));
    /// /
    /// /                details.setMasProcedure(procEntity);
    /// /                details.setProcedureName(req.getProcedureName());
    /// /                details.setRemarks(req.getRemarks());
    /// /                details.setFrequencyId(req.getFrequencyId() != null ? req.getFrequencyId().intValue() : null);
    /// /                details.setNoOfDays(req.getNoOfDays() != null ? req.getNoOfDays().intValue() : null);
    /// /
    /// /                procedureDetailsRepository.save(details);
    /// /            }
    /// /        }
    /// /
    /// /      deleteProcedureCareDetails(request.getRemoveprocedureCareIds());
//
//            log.info("==== END recallOpdPatientDetail ====");
//
//            return ResponseUtils.createSuccessResponse("recall patient update successfully", new TypeReference<>() {});
//
//        }catch (Exception e) {
//            log.error("Error while recall opd", e);
//            throw e;
//        }
//    }
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
        replaceIcdDiagnosis(request, opd, user);
        replaceInvestigations(request, patient, visit, user);
        replaceTreatments(request, patient, visit, user);

        return ResponseUtils.createSuccessResponse("Patient updated successfully", new TypeReference<>() {
        });
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

            Long departmentId = getDepartmentFromInvestigation(inv.getInvestigationId());
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
        String orderNum = createOrderNum();
        LabOrderTrackingStatus labOrderedStatus = labOrderTrackingStatusRepository.findById(orderedStatusId).orElseThrow(() -> new SDDException("status", 500, "Ordered status not found"));

        Map<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> groupedByDate = investigations.stream().filter(inv -> inv.getInvestigationDate() != null).collect(Collectors.groupingBy(RecallOpdPatientDetailRequest.InvestigationRequest::getInvestigationDate));

        for (Map.Entry<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> entry : groupedByDate.entrySet()) {
            LocalDate appointmentDate = entry.getKey();

            // Create order header
            DgOrderHd dgOrderHd = new DgOrderHd();
            dgOrderHd.setAppointmentDate(appointmentDate);
            dgOrderHd.setOrderDate(LocalDate.now());
            dgOrderHd.setOrderTime(Instant.now());
            dgOrderHd.setOrderNo(orderNum);
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
                radOrderDt.setOrderAccessionNo(createOrderNum());
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

//              Billing Details save if need need some more change
//                BillingDetail billingDetail = new BillingDetail();
//                billingDetail.setBillingHd(savedBillingHeader);
//                billingDetail.setBillHd(savedBillingHeader);
//                billingDetail.setItemName(invEntity.getInvestigationName());
//                billingDetail.setQuantity(1);
//                billingDetail.setCreatedDt(OffsetDateTime.now());
//                billingDetail.setUpdatedDt(OffsetDateTime.now());
//                billingDetail.setCreatedAt(Instant.now());
//
//                BigDecimal amount = getInvestigationPrice(invEntity);
//                BigDecimal chargeAmount = amount != null ? amount : BigDecimal.ZERO;
//                billingDetail.setBasePrice(chargeAmount);
//                billingDetail.setTariff(chargeAmount);
//                billingDetail.setDiscount(BigDecimal.ZERO);
//                billingDetail.setAmountAfterDiscount(chargeAmount);
//                billingDetail.setTaxAmount(BigDecimal.ZERO);
//                billingDetail.setTaxPercent(BigDecimal.ZERO);
//                billingDetail.setNetAmount(chargeAmount);
//                billingDetail.setTotal(chargeAmount);
//                billingDetail.setPaymentStatus(AppConstants.STATUS_N.toLowerCase());
//
//                BillingDetail savedBillingDetail = billingDetailRepository.save(billingDetail);
//                log.info("Billing Detail created - Detail ID: {}", savedBillingDetail.getId());
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
                radOrderDt.setOrderAccessionNo(randomNumGenerator.generateOrderNumber("RAD", true, true));
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


    //    Recall Api
//
//    @Override
//    public ApiResponse<OpdPatientRecallResponce> getRecallVisit(Long visitId) {
//
//        Visit visitObj = visitRepository.searchRecallVisits(visitId);
//        Patient patientObj = visitObj.getPatient();
//        User docObj = visitObj.getDoctor();
//        MasDepartment deptObj = visitObj.getDepartment();
//        MasGender genderObj = patientObj.getPatientGender();
//        MasRelation relationObj = patientObj.getPatientRelation();
//
//        OpdPatientDetail opdPatientObj = opdPatientDetailRepository.findByVisitId(visitObj.getId());
//
//        List<DgOrderHd> dgOrderHdList = safeList(dgOrderHdRepo.findAllByVisitId(visitObj));
//
//        PatientPrescriptionHd prescHdObj = patientPrescriptionHdRepository.findByPatientIdAndVisitId(patientObj.getId(),visitId);
//
//        List<PatientPrescriptionDt> prescDtList = prescHdObj != null ? safeList(patientPrescriptionDtRepository
//                        .findByPrescriptionHdId(prescHdObj.getPrescriptionHdId()))
//                        : Collections.emptyList();
//
//        OpdPatientRecallResponce response = new OpdPatientRecallResponce();
//
//        // ---------------- BASIC INFO ----------------
//        response.setPatientId(patientObj.getId());
//        response.setVisitId(visitObj.getId());
//
//        response.setPatientName(
//                buildFullName(
//                        patientObj.getPatientFn(),
//                        patientObj.getPatientMn(),
//                        patientObj.getPatientLn()
//                )
//        );
//
//        response.setMobileNo(patientObj.getPatientMobileNumber());
//
//        response.setGender(
//                genderObj != null ? genderObj.getGenderName() : null
//        );
//
//        response.setRelation(
//                relationObj != null ? relationObj.getRelationName() : null
//        );
//
//        response.setDob(patientObj.getPatientDob());
//        response.setAge(patientObj.getPatientAge());
//
//        response.setDeptId(
//                deptObj != null ? deptObj.getId() : null
//        );
//
//        response.setDeptName(
//                deptObj != null ? deptObj.getDepartmentName() : null
//        );
//
//        response.setDocterId(
//                docObj != null ? docObj.getUserId() : null
//        );
//
//        if (docObj != null) {
//            response.setDocterName(
//                    buildFullName(
//                            docObj.getFirstName(),
//                            docObj.getMiddleName(),
//                            docObj.getLastName()
//                    )
//            );
//        }
//
//        response.setHospitalId(
//                patientObj.getPatientHospital() != null
//                        ? patientObj.getPatientHospital().getId()
//                        : null
//        );
//
//        // ---------------- OPD DETAILS ----------------
//        if (opdPatientObj != null) {
//            mapOpdDetails(response, opdPatientObj);
//            response.setTreatmentAdvice(opdPatientObj.getTreatmentAdvice());
//        }
//
//        // ---------------- DG ORDER ----------------
//        response.setDgOrderHdList(buildDgOrderHdList(dgOrderHdList));
//
//        // ---------------- PRESCRIPTION HD ----------------
//        if (prescHdObj != null) {
//
//            OpdPatientRecallResponce.NewDPatientPrescriptionHd hd =
//                    new OpdPatientRecallResponce.NewDPatientPrescriptionHd();
//
//            hd.setPrescriptionHdId(prescHdObj.getPrescriptionHdId());
//            hd.setStatus(prescHdObj.getStatus());
//            hd.setPrescriptionDate(prescHdObj.getPrescriptionDate());
//
//            response.setPatientPrescriptionHd(hd);
//        }
//
//        // ---------------- PRESCRIPTION DT ----------------
//        List<OpdPatientRecallResponce.NewDPatientPrescriptionDt> newDtList =
//                new ArrayList<>();
//
//        Long hospitalId =
//                authUtil.getCurrentUser() != null
//                        && authUtil.getCurrentUser().getHospital() != null
//                        ? authUtil.getCurrentUser().getHospital().getId()
//                        : null;
//
//        for (PatientPrescriptionDt dt : prescDtList) {
//
//            if (dt == null) continue;
//
//            OpdPatientRecallResponce.NewDPatientPrescriptionDt newDt =
//                    new OpdPatientRecallResponce.NewDPatientPrescriptionDt();
//
//            newDt.setPrescriptionDtId(dt.getPrescriptionDtId());
//            newDt.setPrescriptionHdId(dt.getPrescriptionHdId());
//            newDt.setStatus(dt.getStatus());
//            newDt.setDosage(dt.getDosage());
//            newDt.setFrequency(dt.getFrequency());
//            newDt.setDays(dt.getDays());
//            newDt.setTotal(dt.getTotal());
//            newDt.setInstraction(dt.getInstruction());
//            newDt.setItemId(dt.getItemId());
//            newDt.setFrequencyId(dt.getFrequency());
//
//            Optional<MasStoreItem> itemOpt =
//                    masStoreItemRepository.findById(dt.getItemId());
//
//            itemOpt.ifPresent(item -> {
//
//                newDt.setItemName(item.getNomenclature());
//                newDt.setAdispQty(item.getAdispQty());
//
//                if (item.getDispUnit() != null) {
//                    newDt.setDispUnit(item.getDispUnit().getUnitName());
//                    newDt.setDepUnit(item.getDispUnit().getUnitName());
//                }
//
//                if (item.getItemClassId() != null) {
//                    newDt.setItemClassId(
//                            item.getItemClassId().getItemClassId()
//                    );
//                }
//            });
//
//            Long stocks = 0L;
//
//            if (hospitalId != null && dt.getItemId() != null) {
//
//                Long stockVal = stockFound.getAvailableStocks(
//                        hospitalId,
//                        deptIdStore,
//                        dt.getItemId(),
//                        hospDefinedDays
//                );
//
//                stocks = stockVal != null ? stockVal : 0L;
//            }
//
//            newDt.setStocks(stocks);
//
//            newDtList.add(newDt);
//        }
//
//        response.setPatientPrescriptionDts(newDtList);
//
//        // ---------------- FOLLOW UP / REFERRAL / ADMISSION ----------------
//        if (opdPatientObj != null) {
//
//            response.setFollowUpFlag(opdPatientObj.getFollowUpFlag());
//
//            if (isYes(opdPatientObj.getFollowUpFlag())) {
//                response.setFollowUpDays(opdPatientObj.getFollowUpDays());
//                response.setFollowUpDate(opdPatientObj.getFollowUpDate());
//            }
//
//            response.setReferralFlag(opdPatientObj.getReferralFlag());
//
//            if (isYes(opdPatientObj.getReferralFlag())) {
//                response.setReferralRemarks(opdPatientObj.getReferralRemarks());
//                response.setReferralDate(opdPatientObj.getReferralDate());
//            }
//
//            response.setAdmissionFlag(opdPatientObj.getAdmissionFlag());
//
//            if (isYes(opdPatientObj.getAdmissionFlag())) {
//
//                response.setAdmissionRemarks(
//                        opdPatientObj.getAdmissionRemarks()
//                );
//
//                response.setAdmissionAdvisedDate(
//                        opdPatientObj.getAdmissionAdvisedDate()
//                );
//
//                response.setAdmissionPriority(
//                        opdPatientObj.getAdmissionPriority()
//                );
//
//                if (opdPatientObj.getAdmissionCareLevel() != null) {
//
//                    response.setAdmissionCareLevel(
//                            opdPatientObj.getAdmissionCareLevel().getCareId()
//                    );
//
//                    response.setAdmissionCareLevelName(
//                            opdPatientObj.getAdmissionCareLevel()
//                                    .getCareLevelName()
//                    );
//                }
//
//                if (opdPatientObj.getAdmissionWardCategory() != null) {
//
//                    response.setAdmissionWardCategory(
//                            opdPatientObj.getAdmissionWardCategory().getId()
//                    );
//
//                    response.setAdmissionWardCategoryName(
//                            opdPatientObj.getAdmissionWardCategory()
//                                    .getCategoryName()
//                    );
//                }
//
//                if (opdPatientObj.getAdmissionWard() != null) {
//
//                    response.setAdmissionWard(
//                            opdPatientObj.getAdmissionWard().getId()
//                    );
//
//                    response.setAdmissionWardName(
//                            opdPatientObj.getAdmissionWard()
//                                    .getDepartmentName()
//                    );
//                }
//
//                response.setVacantBed(0);
//                response.setOccupiedBed(0);
//            }
//        }
//
//        return ResponseUtils.createSuccessResponse(response, new TypeReference<>() {});
//    }
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
            Page<PreviousOpdVisitProjection> projectionPage = visitRepository.getPreviousOpdVisit(patientId, hospitalId,AppConstants.STATUS_Y.toLowerCase(), pageable);

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
            Page<PreviousOpdVitalsDetailsProjection> projectionPage = visitRepository.getPriviousOpdVitalsDetails(patientId, hospitalId,AppConstants.STATUS_Y.toLowerCase(), pageable);

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
    private OpdPsychiatryAssessmentHeader savePsychiatricHeaderAndDetails(
            OpdPatientDetailCreateRequest request,
            Visit visit,
            OpdPatientDetail opdPatientDetail) {

        OpdPsychiatryAssessmentHeader header = new OpdPsychiatryAssessmentHeader();

        header.setVisit(visit);
        header.setDepartment(visit.getDepartment());
        header.setPatient(visit.getPatient());
        header.setOpdPatientDetails(opdPatientDetail);
        header.setAssessmentDate(LocalDateTime.now());
        header.setTopic(masQuestionHeadingRepository.findById(request.getTopicId())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Topic not found with id: " + request.getTopicId()
                        ))
        );

        OpdPsychiatryAssessmentHeader savedHeader = opdPsychiatryAssessmentHeaderRepository.save(header);

        BigDecimal totalScore = BigDecimal.ZERO;
        for (OpdPsychiatricDetailsRequest detailReq : request.getDetails()) {

            OpdPsychiatryAssessmentDetail detail = new OpdPsychiatryAssessmentDetail();

            detail.setAssessmentHeaderId(savedHeader);

            detail.setQuestionId(detailReq.getQuestionId() != null
                            ? opdQuestionMasterRepository.findById(detailReq.getQuestionId()).orElse(null)
                            : null);

            MasQuestionOptionValue optionValue = null;
            if (detailReq.getAnswerOptionId() != null) {
                optionValue = masQuestionOptionValueRepository.findById(detailReq.getAnswerOptionId()).orElse(null);
            }
            detail.setAnswerOptionId(optionValue);

            BigDecimal score = BigDecimal.ZERO;
            score = BigDecimal.valueOf(optionValue.getOptionScore());
            detail.setScore(score);
            totalScore = totalScore.add(score);

            opdPsychiatryAssessmentDetailRepository.save(detail);
        }
        savedHeader.setTotalScore(totalScore);

        return opdPsychiatryAssessmentHeaderRepository.save(savedHeader);
    }

    private void handlePregnancyDetails(OpdPatientDetail opd, OpdPatientDetailCreateRequest.PregnancyDetails pregnancyDetails, User user) {
        if (pregnancyDetails == null) {
            return;
        }
        
        // Delete existing pregnancy details if any
        opdPatientPregnancyDetailsRepository.deleteByOpdPatientDetail_OpdPatientDetailsId(opd.getOpdPatientDetailsId());
        
        // Create new pregnancy details
        OpdPatientPregnancyDetails pregnancyEntity = OpdPatientPregnancyDetails.builder()
                .opdPatientDetail(opd)
                .opdPatientDetailsId(opd.getOpdPatientDetailsId())
                .visitId(opd.getVisit() != null ? opd.getVisit().getId() : null)
                .patientId(opd.getPatient() != null ? opd.getPatient().getId() : null)
                .isPregnant(pregnancyDetails.getIsPregnant())
                .lmpDate(pregnancyDetails.getLmpDate())
                .edd(pregnancyDetails.getEdd())
                .currentEdd(pregnancyDetails.getCurrentEdd())
                .gestationPeriod(pregnancyDetails.getGestationPeriod())
                .lastChgDate(Instant.now())
                .lastChgBy(user.getFullName())
                .build();
        
        opdPatientPregnancyDetailsRepository.save(pregnancyEntity);
        log.info("Saved pregnancy details for OPD patient ID: {}", opd.getOpdPatientDetailsId());
    }
}

