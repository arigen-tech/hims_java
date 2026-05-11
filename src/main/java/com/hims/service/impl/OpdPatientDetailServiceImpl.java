package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.constants.AppConstants;
import com.hims.entity.*;
import com.hims.entity.projection.PrescriptionDetailProjection;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.projection.*;
import com.hims.request.ActiveVisitSearchRequest;
import com.hims.request.OpdPatientDetailFinalRequest;
import com.hims.response.*;
import com.hims.service.OpdPatientDetailService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import com.hims.utils.StockFound;
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


    @Override
    @Transactional
    public ApiResponse<OpdPatientDetailResponseDTO> createOpdPatientDetail(OpdPatientDetailFinalRequest request) {
        // ===================== BASIC VALIDATION =====================
        if (request == null) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "Request body cannot be null", 400);
        }
        log.info("Starting createOpdPatientDetail process...");
        log.info("Request Data: {}", request);
        Long deptId = authUtil.getCurrentDepartmentId();
        User useObj = authUtil.getCurrentUser();
        if (useObj == null || useObj.getHospital() == null) {
            throw new SDDException("user", 401, "Authenticated user or hospital not found");
        }

        // ===================== CREATE OR UPDATE =====================
        OpdPatientDetail opdPatientDetail;
        if (request.getOpdPatientDetailId() == null) {
            opdPatientDetail = new OpdPatientDetail();
            log.info("Creating new OpdPatientDetail...");
        } else {
            opdPatientDetail = opdPatientDetailRepository.findById(request.getOpdPatientDetailId()).orElseThrow(() -> new SDDException("opdDetail", 404, "OpdPatientDetail not found with ID: " + request.getOpdPatientDetailId()));
            log.info("Updating OpdPatientDetail ID: {}", request.getOpdPatientDetailId());
        }
        // ========================= VITALS =========================
        opdPatientDetail.setHeight(request.getHeight());
        opdPatientDetail.setIdealWeight(request.getIdealWeight());
        opdPatientDetail.setWeight(request.getWeight());
        opdPatientDetail.setPulse(request.getPulse());
        opdPatientDetail.setTemperature(request.getTemperature());
        opdPatientDetail.setRr(request.getRr());
        opdPatientDetail.setBmi(request.getBmi());
        opdPatientDetail.setSpo2(request.getSpo2());
        opdPatientDetail.setBpSystolic(request.getBpSystolic());
        opdPatientDetail.setBpDiastolic(request.getBpDiastolic());
        opdPatientDetail.setMlcFlag(request.getMlcFlag());
        opdPatientDetail.setFinalMedicalAdvice(request.getDoctorRemarks());

        // ========================= DIAGNOSIS =========================
        if ((request.getWorkingDiagnosis() == null || request.getWorkingDiagnosis().isBlank()) && (request.getIcdDiagnosis() == null || request.getIcdDiagnosis().isEmpty())) {
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
            }, "One is mandatory: Working Diagnosis or ICD Diagnosis", 400);
        }

        opdPatientDetail.setWorkingDiag(request.getWorkingDiagnosis());

        if (request.getIcdDiagnosis() != null && !request.getIcdDiagnosis().isEmpty()) {
            String joinedNames = request.getIcdDiagnosis().stream().filter(Objects::nonNull).map(OpdPatientDetailFinalRequest.IcdDiagnosis::getIcdDiagnosisName).filter(Objects::nonNull).collect(Collectors.joining(","));
            opdPatientDetail.setIcdDiag(joinedNames);
        } else {
            opdPatientDetail.setIcdDiag(null);
        }

        // ==================== CLINICAL HISTORY =====================
        opdPatientDetail.setPastMedicalHistory(request.getPastMedicalHistory());
        opdPatientDetail.setFamilyHistory(request.getFamilyHistory());
        opdPatientDetail.setClinicalExamination(request.getClinicalExamination());
        opdPatientDetail.setPatientSignsSymptoms(request.getPatientSignsSymptoms());

        // ====================== INVESTIGATION ======================
        if (request.getInvestigation() != null && !request.getInvestigation().isEmpty()) {
            if (request.getInvestigation().stream().anyMatch(i -> i == null || i.getInvestigationDate() == null)) {
                throw new SDDException("investigation", 400, "Investigation date cannot be null");
            }
            opdPatientDetail.setLabFlag(request.getLabFlag());
            opdPatientDetail.setRadioFlag(request.getRadioFlag());

            String orderNumOPD = createOrderNum();
            Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new SDDException("patient", 404, "Patient not found"));
            Visit visit = visitRepository.findById(request.getVisitId()).orElseThrow(() -> new SDDException("visit", 404, "Visit not found"));
            LabOrderTrackingStatus labOrderedStatus = labOrderTrackingStatusRepository.findById(orderedStatusId).orElseThrow(() -> new SDDException("status", 500, "Ordered status not found with id: " + orderedStatusId));
            // Separate investigations by category and date
            Map<Long, Map<LocalDate, List<OpdPatientDetailFinalRequest.Investigation>>> groupedByCategory = request.getInvestigation().stream().collect(Collectors.groupingBy(
                    inv -> {
                        // Get department for each investigation
                        long departmentId = getDepartmentFromInvestigation(inv.getId());
                        log.debug("Investigation ID: {} belongs to department: {}",
                                inv.getId(), departmentId);
                        return departmentId;
                    },
                    Collectors.groupingBy(OpdPatientDetailFinalRequest.Investigation::getInvestigationDate)
            ));

            log.info("Investigation categories found: {}", groupedByCategory.keySet());


        // Process LAB investigations
            if (laboratoryDepartment != null && groupedByCategory.containsKey(Long.valueOf(laboratoryDepartment))) {
                log.info("Processing LAB investigations");
                processLabInvestigations(groupedByCategory.get(Long.valueOf(laboratoryDepartment)), patient, visit, useObj, deptId, orderNumOPD, labOrderedStatus);
            } else {
                log.warn("Laboratory department {} not found in investigation categories. Available: {}",
                        laboratoryDepartment, groupedByCategory.keySet());
            }

        // Process RADIOLOGY investigations
            if (radiologyDepartment != null && groupedByCategory.containsKey(Long.valueOf(radiologyDepartment))) {
                log.info("Processing RADIOLOGY investigations");
                processRadiologyInvestigations(groupedByCategory.get(Long.valueOf(radiologyDepartment)), patient, visit, useObj);
            } else {
                log.warn("Radiology department {} not found in investigation categories. Available: {}",
                        radiologyDepartment, groupedByCategory.keySet());
            }
        }

        // ======================== TREATMENT ========================
        opdPatientDetail.setTreatmentAdvice(request.getTreatmentAdvice());
        if (request.getTreatment() != null && !request.getTreatment().isEmpty()) {
            Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new SDDException("patient", 404, "Patient not found"));

            PatientPrescriptionHd hd = new PatientPrescriptionHd();
            hd.setHospitalId(useObj.getHospital().getId());
            hd.setPatientId(patient.getId());
            hd.setDepartmentId(deptId);
            hd.setDoctorName(useObj.getFirstName());
            hd.setPrescriptionDate(LocalDateTime.now());
            hd.setStatus(AppConstants.STATUS_N.toLowerCase());
            hd.setBillingStatus(AppConstants.PAYMENT_NOT_PAID.toLowerCase().equalsIgnoreCase(useObj.getHospital().getMedicineBilling()) ? "n" : "y");
            hd.setCreatedBy(useObj.getFirstName());
            hd.setTotalCost(BigDecimal.ZERO);
            hd.setTotalGst(BigDecimal.ZERO);
            hd.setTotalDiscount(BigDecimal.ZERO);
            hd.setNetAmount(BigDecimal.ZERO);

            PatientPrescriptionHd savedHd = patientPrescriptionHdRepository.save(hd);

            for (OpdPatientDetailFinalRequest.Treatment trt : request.getTreatment()) {
                if (trt == null) continue;

                PatientPrescriptionDt dt = new PatientPrescriptionDt();
                dt.setPrescriptionHdId(savedHd.getPrescriptionHdId());
                dt.setItemId(trt.getItemId());
                dt.setDosage(trt.getDosage());
                dt.setFrequency(trt.getFrequency());
                dt.setDays(trt.getDays());
                dt.setTotal(trt.getTotal());
                dt.setInstruction(trt.getInstraction());
                dt.setStatus(AppConstants.STATUS_N.toLowerCase());

                patientPrescriptionDtRepository.save(dt);
            }
        }

        // ====================== GENERAL DETAILS =====================
        opdPatientDetail.setOpdDate(Instant.now());
        opdPatientDetail.setPatient(patientRepository.findById(request.getPatientId()).orElseThrow(() -> new SDDException("patient", 404, "Patient not found")));
        opdPatientDetail.setVisit(visitRepository.findById(request.getVisitId()).orElseThrow(() -> new SDDException("visit", 404, "Visit not found")));
        opdPatientDetail.setDepartment(departmentRepository.findById(deptId).orElseThrow(() -> new SDDException("department", 404, "Department not found")));
        opdPatientDetail.setHospital(hospitalRepository.findById(useObj.getHospital().getId()).orElseThrow(() -> new SDDException("hospital", 404, "Hospital not found")));
        opdPatientDetail.setDoctor(userRepository.findById(useObj.getUserId()).orElseThrow(() -> new SDDException("doctor", 404, "Doctor not found")));

        opdPatientDetail.setLastChgBy(useObj.getUsername());
        opdPatientDetail.setLastChgDate(Instant.now());

        // ========================= Admission Advice =====================================

        if (isYes(request.getAdmissionFlag())) {
            masCareLevelRepository.findById(request.getAdmissionCareLevel()).ifPresent(opdPatientDetail::setAdmissionCareLevel);
            masWardCategoryRepository.findById(request.getAdmissionWardCategory()).ifPresent(opdPatientDetail::setAdmissionWardCategory);
            masDepartmentRepository.findById(request.getAdmissionWard()).ifPresent(opdPatientDetail::setAdmissionWard);
            opdPatientDetail.setAdmissionFlag(AppConstants.STATUS_Y.toLowerCase());
            opdPatientDetail.setAdmissionAdvisedDate(request.getAdmissionAdvisedDate());
            opdPatientDetail.setAdmissionRemarks(request.getAdmissionRemarks());
            opdPatientDetail.setAdmissionPriority(request.getAdmissionPriority());
        } else {
            opdPatientDetail.setAdmissionFlag(AppConstants.STATUS_N.toLowerCase());
        }

        // ========================= Follow up =========================
        if (isYes(request.getFollowUpFlag())) {
            opdPatientDetail.setFollowUpFlag(AppConstants.STATUS_Y.toLowerCase());
            opdPatientDetail.setFollowUpDays(request.getFollowUpDays());
            opdPatientDetail.setFollowUpDate(request.getFollowUpDate());
        } else {
            opdPatientDetail.setFollowUpFlag(AppConstants.STATUS_N.toLowerCase());
        }

        //  =========================== referral ==============================
        opdPatientDetail.setReferralFlag(isYes(request.getReferralFlag()) ? AppConstants.STATUS_Y.toLowerCase() : AppConstants.STATUS_N.toLowerCase());
        opdPatientDetail.setReferralRemarks(request.getReferralRemarks());
        opdPatientDetail.setReferredHospitalName(request.getReferredHospitalName());
        opdPatientDetail.setReferralDate(request.getReferralDate());
        opdPatientDetail.setReferTo(request.getReferTo());


        // ====================== SAVE OPD ============================
        OpdPatientDetail saved = opdPatientDetailRepository.save(opdPatientDetail);

        // ====================== ICD SAVE (NO DUPLICATES) ============
        if (request.getIcdDiagnosis() != null && !request.getIcdDiagnosis().isEmpty()) {

            dischargeIcdCodeRepository.deleteByOpdPatientDetailsId(saved.getOpdPatientDetailsId());
            for (OpdPatientDetailFinalRequest.IcdDiagnosis icd : request.getIcdDiagnosis()) {
                if (icd == null) continue;
                DischargeIcdCode code = new DischargeIcdCode();
                code.setIcdId(icd.getIcdId());
                code.setOpdPatientDetailsId(saved.getOpdPatientDetailsId());
                code.setVisitId(request.getVisitId());
                code.setAddEditById(useObj.getUserId());
                code.setAddEditDate(LocalDate.now());
                code.setAddEditTime(LocalTime.now().toString());

                dischargeIcdCodeRepository.save(code);
            }
        }

        // ================================ Procedure Care =====================
//        if (request.getProcedureCare() != null && !request.getProcedureCare().isEmpty()) {
//
//            log.info("Creating Procedure Header & Procedure Details...");
//
//            Patient patObj = patientRepository.findById(request.getPatientId())
//                    .orElseThrow(() -> new RuntimeException("Patient not found"));
//
//            Visit visitObj = visitRepository.findById(request.getVisitId())
//                    .orElseThrow(() -> new RuntimeException("Visit not found"));
//
//            MasHospital hosObj = useObj.getHospital();
//
//            // *************** CREATE HEADER ***************
//            ProcedureHeader header = new ProcedureHeader();
//            header.setStatus("n");
//            header.setLastChangedDate(LocalDate.now());
//            header.setLastChangedTime(LocalTime.now().toString());
//            header.setRequisitionDate(LocalDate.now());
//            header.setProcedureDate(LocalDateTime.now());
//            header.setProcedureTime(LocalTime.now().toString());
//            header.setHinId(Math.toIntExact(patObj.getId()));
//            header.setHospital(hosObj);
//            header.setLastChangedBy(Math.toIntExact(useObj.getUserId()));
//            header.setMedicalOfficerId(Math.toIntExact(useObj.getUserId()));
//            header.setVisitId(Math.toIntExact(visitObj.getId()));
//            header.setOpdPatientDetailsId(Math.toIntExact(saved.getOpdPatientDetailsId()));
//            header.setProcedureType("OPD"); // OPD procedure
//
//            ProcedureHeader savedHeader = procedureHeaderRepository.save(header);
//
//
//            // *************** CREATE MULTIPLE DETAILS ***************
//            for (OpdPatientDetailFinalRequest.ProcedureCare req : request.getProcedureCare()) {
//
//                MasProcedure procEntity = masProcedureRepository.findById(req.getProcedureId())
//                        .orElseThrow(() ->
//                                new RuntimeException("Procedure not found with ID: " + req.getProcedureId()));
//
//                ProcedureDetails details = ProcedureDetails.builder()
//                        .procedureHeader(savedHeader)
//                        .remarks(req.getRemarks())
//                        .procedureName(req.getProcedureName())
//                        .status("n")
//                        .masProcedure(procEntity)
//                        .frequencyId(req.getFrequencyId() != null ? req.getFrequencyId().intValue() : null)
//                        .noOfDays(req.getNoOfDays() != null ? req.getNoOfDays().intValue() : null)
//                        .appointmentDate(LocalDate.now())
//                        .finalProcedureStatus("n")
//                        .nursingRemark(null)
//                        .nextAppointmentDate(null)
//                        .appointmentTime(LocalTime.now().toString())
//                        .procedureDate(LocalDate.now())
//                        .procedureTime(LocalTime.now().toString())
//                        .build();
//
//                procedureDetailsRepository.save(details);
//            }
//        }
//
        // ====================== VISIT CLOSE =========================
        Visit visit = visitRepository.findById(request.getVisitId()).orElseThrow(() -> new SDDException("visit", 404, "Visit not found"));
        visit.setVisitStatus(AppConstants.VISIT_STATUS_CLOSED.toLowerCase());
        visitRepository.save(visit);

        return ResponseUtils.createSuccessResponse(null, new TypeReference<>() {
        });
    }

    private Long getDepartmentFromInvestigation(Long investigationId) {
        DgMasInvestigation investigation = dgMasInvestigationRepository.findById(investigationId)
                .orElseThrow(() -> new SDDException("investigation", 404,
                        "Investigation not found with ID: " + investigationId));

        MasSubChargeCode subChargeCode = subChargeCodeRepository.findById(investigation.getSubChargeCodeId().getSubId())
                .orElseThrow(() -> new SDDException("subcharge", 404,
                        "Subcharge code not found for investigation ID: " + investigationId));


        return subChargeCode.getMasDepartment().getId();
    }

    @Transactional
    @Override
    public ApiResponse<OpdPatientDetail> recallOpdPatientDetail(RecallOpdPatientDetailRequest request) {

        // ===================== BASIC VALIDATION =====================
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }

        Objects.requireNonNull(request.getOpdPatientId(), "OPD Patient ID is required");
        Objects.requireNonNull(request.getPatientId(), "Patient ID is required");
        Objects.requireNonNull(request.getVisitId(), "Visit ID is required");
        Objects.requireNonNull(request.getDepartmentId(), "Department ID is required");
        Objects.requireNonNull(request.getHospitalId(), "Hospital ID is required");

        User useObj = authUtil.getCurrentUser();
        if (useObj == null || useObj.getHospital() == null) {
            throw new RuntimeException("Authenticated user or hospital not found");
        }

        // ===================== FETCH OPD =====================
        OpdPatientDetail opdObj = opdPatientDetailRepository.findById(request.getOpdPatientId()).orElseThrow(() -> new RuntimeException("OPD record not found: " + request.getOpdPatientId()));

        // ===================== UPDATE VITALS =====================
        opdObj.setHeight(request.getHeight());
        opdObj.setWeight(request.getWeight());
        opdObj.setTemperature(request.getTemperature());
        opdObj.setBpDiastolic(request.getDiastolicBP());
        opdObj.setBpSystolic(request.getSystolicBP());
        opdObj.setPulse(request.getPulse());
        opdObj.setBmi(request.getBmi());
        opdObj.setRr(request.getRr());
        opdObj.setSpo2(request.getSpo2());
        opdObj.setPatientSignsSymptoms(request.getPatientSymptoms());
        opdObj.setClinicalExamination(request.getClinicalExamination());
        opdObj.setPastMedicalHistory(request.getPastHistory());
        opdObj.setFamilyHistory(request.getFamilyHistory());
        opdObj.setMlcFlag(request.getMlcCase());
        opdObj.setWorkingDiag(request.getWorkingDiagnosis());
        opdObj.setIcdDiag(request.getIcdDiagnosis());
        opdObj.setFinalMedicalAdvice(request.getDoctorRemarks());

        // ===================== ADMISSION =====================
        if (isYes(request.getAdmissionFlag())) {

            opdObj.setAdmissionFlag("y");

            if (request.getAdmissionCareLevel() != null) {
                masCareLevelRepository.findById(request.getAdmissionCareLevel()).ifPresent(opdObj::setAdmissionCareLevel);
            }

            if (request.getAdmissionWardCategory() != null) {
                masWardCategoryRepository.findById(request.getAdmissionWardCategory()).ifPresent(opdObj::setAdmissionWardCategory);
            }

            if (request.getAdmissionWard() != null) {
                masDepartmentRepository.findById(request.getAdmissionWard()).ifPresent(opdObj::setAdmissionWard);
            }

            opdObj.setAdmissionAdvisedDate(request.getAdmissionAdvisedDate());
            opdObj.setAdmissionRemarks(request.getAdmissionRemarks());
            opdObj.setAdmissionPriority(request.getAdmissionPriority());

        } else {
            opdObj.setAdmissionFlag("n");

            opdObj.setAdmissionCareLevel(null);
            opdObj.setAdmissionWardCategory(null);
            opdObj.setAdmissionWard(null);
            opdObj.setAdmissionAdvisedDate(null);
            opdObj.setAdmissionRemarks(null);
            opdObj.setAdmissionPriority(null);
        }


        // ===================== FOLLOW UP =====================
        if (isYes(request.getFollowUpFlag())) {

            opdObj.setFollowUpFlag("y");
            opdObj.setFollowUpDays(request.getFollowUpDays());
            opdObj.setFollowUpDate(request.getFollowUpDate());

        } else {
            opdObj.setFollowUpFlag("n");
            opdObj.setFollowUpDays(null);
            opdObj.setFollowUpDate(null);
        }


        // ===================== Rrferral =====================

        if (isYes(request.getReferralFlag())) {

            opdObj.setReferralFlag("y");
            opdObj.setReferralRemarks(request.getReferralRemarks());
            opdObj.setReferralDate(request.getReferralDate());

        } else {
            opdObj.setReferralFlag("n");
            opdObj.setReferralRemarks(null);
            opdObj.setReferralDate(null);
        }


        OpdPatientDetail saved = opdPatientDetailRepository.save(opdObj);

        // ===================== INVESTIGATIONS =====================
        opdObj.setLabFlag(request.getLabFlag());
        opdObj.setRadioFlag(request.getRadioFlag());
        List<RecallOpdPatientDetailRequest.InvestigationRequest> invList = request.getInvestigations();

        if (invList != null && !invList.isEmpty()) {

            String orderNumOPD = createOrderNum();

            Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));

            Visit visit = visitRepository.findById(request.getVisitId()).orElseThrow(() -> new RuntimeException("Visit not found"));

            Map<LocalDate, DgOrderHd> existingHdByDate = new HashMap<>();

            List<DgOrderHd> existingHdList = dgOrderHdRepo.findAllByVisitId(visit);
            if (existingHdList != null) {
                for (DgOrderHd hd : existingHdList) {
                    if (hd != null && hd.getAppointmentDate() != null) {
                        existingHdByDate.put(hd.getAppointmentDate(), hd);
                    }
                }
            }

            Map<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> grouped = invList.stream().filter(Objects::nonNull).filter(i -> i.getId() == null).filter(i -> i.getDate() != null).collect(Collectors.groupingBy(RecallOpdPatientDetailRequest.InvestigationRequest::getDate));

            for (Map.Entry<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> entry : grouped.entrySet()) {

                DgOrderHd dgOrderHd = existingHdByDate.computeIfAbsent(entry.getKey(), date -> {
                    DgOrderHd hd = new DgOrderHd();
                    hd.setOrderDate(LocalDate.now());
                    hd.setAppointmentDate(date);
                    hd.setOrderNo(orderNumOPD);
                    hd.setOrderStatus("n");
                    hd.setCollectionStatus("n");
                    hd.setPaymentStatus("n");
                    hd.setSource("OPD PATIENT");
                    hd.setDiscountId(1);
                    hd.setPatientId(patient);
                    hd.setDepartmentId(request.getDepartmentId());
                    hd.setHospitalId(request.getHospitalId());
                    hd.setVisitId(visit);
                    hd.setCreatedBy(useObj.getFirstName());
                    hd.setLastChgBy(useObj.getFirstName());
                    hd.setCreatedOn(LocalDate.now());
                    hd.setLastChgDate(LocalDate.now());
                    hd.setLastChgTime(LocalTime.now().toString());
                    return dgOrderHdRepo.save(hd);
                });

                for (RecallOpdPatientDetailRequest.InvestigationRequest inv : entry.getValue()) {

                    if (inv.getInvestigationId() == null) continue;

                    DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(inv.getInvestigationId()).orElseThrow(() -> new RuntimeException("Investigation not found: " + inv.getInvestigationId()));

                    DgOrderDt dt = new DgOrderDt();
                    dt.setOrderhdId(dgOrderHd);
                    dt.setInvestigationId(invEntity);
                    dt.setAppointmentDate(inv.getDate());
                    dt.setOrderQty(1);
                    dt.setOrderStatus("n");
                    dt.setBillingStatus("n");
                    dt.setCreatedBy(useObj.getFirstName());
                    dt.setLastChgBy(useObj.getFirstName());
                    dt.setCreatedon(Instant.now());
                    dt.setLastChgDate(LocalDate.now());
                    dt.setLastChgTime(LocalTime.now().toString());

                    if (invEntity.getMainChargeCodeId() != null) {
                        dt.setMainChargecodeId(invEntity.getMainChargeCodeId().getChargecodeId());
                    }
                    if (invEntity.getSubChargeCodeId() != null) {
                        dt.setSubChargeid(invEntity.getSubChargeCodeId().getSubId());
                    }

                    dgOrderDtRepo.save(dt);
                }
            }
        }

        // ===================== TREATMENT =====================
        opdObj.setTreatmentAdvice(request.getTreatmentAdvice());
        List<RecallOpdPatientDetailRequest.TreatmentRequest> treatments = request.getTreatments();

        if (treatments != null && !treatments.isEmpty()) {

            Long prescriptionHdId;

            Optional<RecallOpdPatientDetailRequest.TreatmentRequest> existing = treatments.stream().filter(t -> t != null && t.getTreatmentId() != null).findFirst();

            if (existing.isPresent()) {
                PatientPrescriptionDt dt = patientPrescriptionDtRepository.findById(existing.get().getTreatmentId()).orElseThrow(() -> new RuntimeException("Treatment not found"));
                prescriptionHdId = dt.getPrescriptionHdId();
            } else {

                patientPrescriptionHdRepository.findLatestByPatientId(request.getPatientId()).ifPresent(hd -> {
                    patientPrescriptionDtRepository.deleteByPrescriptionHdId(hd.getPrescriptionHdId());
                    patientPrescriptionHdRepository.deleteById(hd.getPrescriptionHdId());
                });

                Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new RuntimeException("Patient not found"));

                PatientPrescriptionHd hd = new PatientPrescriptionHd();
                hd.setHospitalId(useObj.getHospital().getId());
                hd.setPatientId(patient.getId());
                hd.setDepartmentId(request.getDepartmentId());
                hd.setDoctorName(useObj.getFirstName());
                hd.setPrescriptionDate(LocalDateTime.now());
                hd.setStatus("n");
                hd.setCreatedBy(useObj.getFirstName());
                hd.setTotalCost(BigDecimal.ZERO);
                hd.setTotalGst(BigDecimal.ZERO);
                hd.setTotalDiscount(BigDecimal.ZERO);
                hd.setNetAmount(BigDecimal.ZERO);

                prescriptionHdId = patientPrescriptionHdRepository.save(hd).getPrescriptionHdId();
            }

            Long finalHdId = prescriptionHdId;

            treatments.stream().filter(t -> t != null && t.getTreatmentId() == null).forEach(trt -> {
                PatientPrescriptionDt dt = new PatientPrescriptionDt();
                dt.setPrescriptionHdId(finalHdId);
                dt.setItemId(trt.getDrugId());
                dt.setDosage(trt.getDosage());
                dt.setFrequency(trt.getFrequency());
                dt.setDays(trt.getDays());
                dt.setTotal(trt.getTotal() == null ? BigDecimal.ZERO : BigDecimal.valueOf(trt.getTotal()));
                dt.setInstruction(trt.getInstruction());
                dt.setStatus("n");
                patientPrescriptionDtRepository.save(dt);
            });
        }

        // ===================== ICD =====================
        if (request.getIcdObj() != null) {
            for (RecallOpdPatientDetailRequest.IcdDiagnosis icd : request.getIcdObj()) {
                if (icd == null) continue;

                if (icd.getId() == null) {
                    DischargeIcdCode newIcd = new DischargeIcdCode();
                    newIcd.setIcdId(icd.getIcdId());
                    newIcd.setOpdPatientDetailsId(saved.getOpdPatientDetailsId());
                    newIcd.setVisitId(request.getVisitId());
                    newIcd.setAddEditById(useObj.getUserId());
                    newIcd.setAddEditDate(LocalDate.now());
                    newIcd.setAddEditTime(LocalTime.now().toString());
                    dischargeIcdCodeRepository.save(newIcd);
                } else {
                    DischargeIcdCode existing = dischargeIcdCodeRepository.findById(icd.getId()).orElseThrow(() -> new RuntimeException("ICD not found"));
                    existing.setIcdId(icd.getIcdId());
                    existing.setAddEditById(useObj.getUserId());
                    existing.setAddEditDate(LocalDate.now());
                    existing.setAddEditTime(LocalTime.now().toString());
                    dischargeIcdCodeRepository.save(existing);
                }
            }
        }


        // ================================ Procedure Care =====================
//        List<RecallOpdPatientDetailRequest.ProcedureCare> careList = request.getProcedureCare();
//
//        boolean allIdsNull = careList.stream()
//                .allMatch(c -> c.getId() == null);
//
//        ProcedureHeader existingHeader =
//                procedureHeaderRepository.findByVisitId(request.getVisitId())
//                        .orElse(null);
//        if (allIdsNull && existingHeader != null) {
//
//            procedureDetailsRepository.deleteByProcedureHeader(existingHeader);
//
//            procedureHeaderRepository.delete(existingHeader);
//
//            existingHeader = null;
//        }
//
//        ProcedureHeader header = existingHeader;
//
//        if (header == null) {
//            header = new ProcedureHeader();
//            header.setStatus("n");
//            header.setLastChangedDate(LocalDate.now());
//            header.setLastChangedTime(LocalTime.now().toString());
//            header.setRequisitionDate(LocalDate.now());
//            header.setProcedureDate(LocalDateTime.now());
//            header.setProcedureTime(LocalTime.now().toString());
//            header.setHinId(Math.toIntExact(request.getOpdPatientId()));
//            header.setHospital(useObj.getHospital());
//            header.setLastChangedBy(Math.toIntExact(useObj.getUserId()));
//            header.setMedicalOfficerId(Math.toIntExact(useObj.getUserId()));
//            header.setVisitId(Math.toIntExact(request.getVisitId()));
//            header.setOpdPatientDetailsId(Math.toIntExact(saved.getOpdPatientDetailsId()));
//            header.setProcedureType("OPD");
//
//            header = procedureHeaderRepository.save(header);
//        }
//
//        for (RecallOpdPatientDetailRequest.ProcedureCare req : careList) {
//
//            MasProcedure procEntity = masProcedureRepository.findById(req.getProcedureId())
//                    .orElseThrow(() ->
//                            new RuntimeException("Procedure not found with ID: " + req.getProcedureId()));
//
//            if (req.getId() == null) {
//                // ===== CREATE =====
//                ProcedureDetails details = new ProcedureDetails();
//                details.setProcedureHeader(header);
//                details.setMasProcedure(procEntity);
//                details.setProcedureName(req.getProcedureName());
//                details.setRemarks(req.getRemarks());
//                details.setFrequencyId(req.getFrequencyId() != null ? req.getFrequencyId().intValue() : null);
//                details.setNoOfDays(req.getNoOfDays() != null ? req.getNoOfDays().intValue() : null);
//                details.setStatus("n");
//                details.setFinalProcedureStatus("n");
//                details.setAppointmentDate(LocalDate.now());
//                details.setAppointmentTime(LocalTime.now().toString());
//                details.setProcedureDate(LocalDate.now());
//                details.setProcedureTime(LocalTime.now().toString());
//
//                procedureDetailsRepository.save(details);
//
//            } else {
//                // ===== UPDATE =====
//                ProcedureDetails details = procedureDetailsRepository.findById(req.getId())
//                        .orElseThrow(() ->
//                                new RuntimeException("Procedure detail not found: " + req.getId()));
//
//                details.setMasProcedure(procEntity);
//                details.setProcedureName(req.getProcedureName());
//                details.setRemarks(req.getRemarks());
//                details.setFrequencyId(req.getFrequencyId() != null ? req.getFrequencyId().intValue() : null);
//                details.setNoOfDays(req.getNoOfDays() != null ? req.getNoOfDays().intValue() : null);
//
//                procedureDetailsRepository.save(details);
//            }
//        }
//


        // ===================== DELETE REMOVED =====================
        deleteDischargeIcd(request.getRemoveIcdIds());
        deleteOrderDetails(request.getRemovedInvestigationIds());
        deletePrescriptionDetails(request.getRemovedTreatmentIds());
        //        deleteProcedureCareDetails(request.getRemoveprocedureCareIds());


        return ResponseUtils.createSuccessResponse(saved, new TypeReference<>() {
        });
    }

    private boolean isYes(String flag) {
        return flag != null && flag.equalsIgnoreCase("y");
    }

    /**
     * Process LAB investigations for OPD patient
     * Creates DgOrderHd and DgOrderDt records
     */
    private void processLabInvestigations(Map<LocalDate, List<OpdPatientDetailFinalRequest.Investigation>> groupedByDate, Patient patient, Visit visit, User currentUser, Long deptId, String orderNum, LabOrderTrackingStatus labOrderedStatus) {

        log.info("Starting LAB investigation processing for patient ID: {}", patient.getId());

        for (Map.Entry<LocalDate, List<OpdPatientDetailFinalRequest.Investigation>> entry : groupedByDate.entrySet()) {
            LocalDate appointmentDate = entry.getKey();
            List<OpdPatientDetailFinalRequest.Investigation> investigations = entry.getValue();

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
//                for (OpdPatientDetailFinalRequest.Investigation inv : investigations) {
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
            for (OpdPatientDetailFinalRequest.Investigation invObj : investigations) {
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
    private void processRadiologyInvestigations(Map<LocalDate, List<OpdPatientDetailFinalRequest.Investigation>> groupedByDate, Patient patient, Visit visit, User currentUser) {
        log.info("Starting RADIOLOGY investigation processing for patient ID: {}", patient.getId());
        MasServiceCategory radiologyServiceCategory = masServiceCategoryRepository.findByServiceCateCode("SC004");
        if (radiologyServiceCategory == null) {
            log.error("Radiology service category (SC004) not found");
            throw new SDDException("serviceCategory", 400, "Radiology service category not configured");
        }

        for (Map.Entry<LocalDate, List<OpdPatientDetailFinalRequest.Investigation>> entry : groupedByDate.entrySet()) {
            LocalDate appointmentDate = entry.getKey();
            List<OpdPatientDetailFinalRequest.Investigation> investigations = entry.getValue();

            log.debug("Processing {} RADIOLOGY investigations for date: {}", investigations.size(), appointmentDate);

            // Calculate totals for radiology investigations
            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal discountAmount = BigDecimal.ZERO;
            BigDecimal taxAmount = BigDecimal.ZERO;

            for (OpdPatientDetailFinalRequest.Investigation inv : investigations) {
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
            for (OpdPatientDetailFinalRequest.Investigation invObj : investigations) {
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


    @Transactional
    public void deleteOrderDetails(List<Integer> removedOrderDtIds) {
        if (removedOrderDtIds == null || removedOrderDtIds.isEmpty()) return;

        for (Integer dtId : new ArrayList<>(removedOrderDtIds)) {
            if (dtId == null) continue;

            DgOrderDt orderDt = dgOrderDtRepo.findById(dtId).orElseThrow(() -> new RuntimeException("OrderDt not found: " + dtId));

            Integer orderHdId = orderDt.getOrderhdId() != null ? orderDt.getOrderhdId().getId() : null;
            if (orderHdId == null) {
                dgOrderDtRepo.deleteById(dtId);
                continue;
            }

            List<DgOrderDt> allOrderDts = dgOrderDtRepo.findByOrderhdIdId(orderHdId);

            if (allOrderDts == null || allOrderDts.isEmpty()) {
                dgOrderDtRepo.deleteById(dtId);
                dgOrderHdRepo.findById(orderHdId).ifPresent(h -> dgOrderHdRepo.deleteById(orderHdId));
                continue;
            }

            if (allOrderDts.size() == 1) {
                dgOrderDtRepo.deleteById(dtId);
                dgOrderHdRepo.deleteById(orderHdId);
                continue;
            }

            List<Integer> allDtIds = allOrderDts.stream().map(DgOrderDt::getId).toList();

            boolean allPresentInRemoveList = removedOrderDtIds.containsAll(allDtIds);

            if (allPresentInRemoveList) {
                dgOrderDtRepo.deleteAll(allOrderDts);
                dgOrderHdRepo.deleteById(orderHdId);
            } else {
                if (removedOrderDtIds.contains(dtId)) {
                    dgOrderDtRepo.deleteById(dtId);
                }
            }
        }
    }

    @Transactional
    public void deletePrescriptionDetails(List<Long> removedPrescriptionDtIds) {
        if (removedPrescriptionDtIds == null || removedPrescriptionDtIds.isEmpty()) return;

        for (Long dtId : new ArrayList<>(removedPrescriptionDtIds)) {
            if (dtId == null) continue;

            PatientPrescriptionDt dt = patientPrescriptionDtRepository.findById(dtId).orElseThrow(() -> new RuntimeException("PrescriptionDt not found: " + dtId));

            Long hdId = dt.getPrescriptionHdId();
            if (hdId == null) {
                patientPrescriptionDtRepository.deleteById(dtId);
                continue;
            }

            List<PatientPrescriptionDt> allDtOfHd = patientPrescriptionDtRepository.findByPrescriptionHdId(hdId);
            if (allDtOfHd == null || allDtOfHd.isEmpty()) {
                patientPrescriptionDtRepository.deleteById(dtId);
                patientPrescriptionHdRepository.findById(hdId).ifPresent(h -> patientPrescriptionHdRepository.deleteById(hdId));
                continue;
            }

            if (allDtOfHd.size() == 1) {
                patientPrescriptionDtRepository.deleteById(dtId);
                patientPrescriptionHdRepository.deleteById(hdId);
                continue;
            }

            List<Long> allDtIds = allDtOfHd.stream().map(PatientPrescriptionDt::getPrescriptionDtId).toList();

            boolean allDtPresentInRemoveList = removedPrescriptionDtIds.containsAll(allDtIds);

            if (allDtPresentInRemoveList) {
                patientPrescriptionDtRepository.deleteAll(allDtOfHd);
                patientPrescriptionHdRepository.deleteById(hdId);
            } else {
                if (removedPrescriptionDtIds.contains(dtId)) {
                    patientPrescriptionDtRepository.deleteById(dtId);
                }
            }
        }
    }


    @Transactional
    public void deleteDischargeIcd(List<Long> removedICDIds) {

        if (removedICDIds == null || removedICDIds.isEmpty()) {
            return;
        }

        dischargeIcdCodeRepository.deleteAllByIdInBatch(removedICDIds);
    }

    @Transactional
    public void deleteProcedureCareDetails(List<Long> procedureDetailsIds) {

        if (procedureDetailsIds == null || procedureDetailsIds.isEmpty()) {
            return;
        }

        procedureDetailsRepository.deleteAllByIdInBatch(procedureDetailsIds);
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

    @Override
    public ApiResponse<List<OpdPatientRecallResponce>> getRecallVisit(String name, String mobile, LocalDate visitDate) {

        if (visitDate == null && isEmpty(mobile) && isEmpty(name)) {
            visitDate = LocalDate.now();
        }

        mobile = safeString(mobile);
        name = safeString(name);

        List<Visit> recallVisit = safeList(visitRepository.searchRecallVisits(visitDate, mobile, name));

        List<OpdPatientRecallResponce> responseList = new ArrayList<>();

        for (Visit visitObj : recallVisit) {

            if (visitObj == null || visitObj.getPatient() == null) {
                continue;
            }

            Patient patientObj = visitObj.getPatient();
            User docObj = visitObj.getDoctor();
            MasDepartment deptObj = visitObj.getDepartment();
            MasGender genderObj = patientObj.getPatientGender();
            MasRelation relationObj = patientObj.getPatientRelation();

            OpdPatientDetail opdPatientObj = opdPatientDetailRepository.findByVisitId(visitObj.getId());

            List<DgOrderHd> dgOrderHdList = safeList(dgOrderHdRepo.findAllByVisitId(visitObj));

            PatientPrescriptionHd prescHdObj = patientPrescriptionHdRepository.findByPatientId(patientObj.getId());

            List<PatientPrescriptionDt> prescDtList = prescHdObj != null ? safeList(patientPrescriptionDtRepository.findByPrescriptionHdId(prescHdObj.getPrescriptionHdId())) : Collections.emptyList();

            OpdPatientRecallResponce response = new OpdPatientRecallResponce();

            // ---------------- BASIC INFO ----------------
            response.setPatientId(patientObj.getId());
            response.setVisitId(visitObj.getId());
            response.setPatientName(buildFullName(patientObj.getPatientFn(), patientObj.getPatientMn(), patientObj.getPatientLn()));
            response.setMobileNo(patientObj.getPatientMobileNumber());
            response.setGender(genderObj != null ? genderObj.getGenderName() : null);
            response.setRelation(relationObj != null ? relationObj.getRelationName() : null);
            response.setDob(patientObj.getPatientDob());
            response.setAge(patientObj.getPatientAge());
            response.setDeptId(deptObj != null ? deptObj.getId() : null);
            response.setDeptName(deptObj != null ? deptObj.getDepartmentName() : null);
            response.setDocterId(docObj != null ? docObj.getUserId() : null);

            if (docObj != null) {
                response.setDocterName(buildFullName(docObj.getFirstName(), docObj.getMiddleName(), docObj.getLastName()));
            }

            response.setHospitalId(patientObj.getPatientHospital() != null ? patientObj.getPatientHospital().getId() : null);

            // ---------------- OPD DETAILS ----------------
            if (opdPatientObj != null) {
                mapOpdDetails(response, opdPatientObj);
                response.setTreatmentAdvice(opdPatientObj.getTreatmentAdvice());
            }

            // ---------------- DG ORDER ----------------
            response.setDgOrderHdList(buildDgOrderHdList(dgOrderHdList));

            // ---------------- PRESCRIPTION HD ----------------
            if (prescHdObj != null) {
                OpdPatientRecallResponce.NewDPatientPrescriptionHd hd = new OpdPatientRecallResponce.NewDPatientPrescriptionHd();

                hd.setPrescriptionHdId(prescHdObj.getPrescriptionHdId());
                hd.setStatus(prescHdObj.getStatus());
                hd.setPrescriptionDate(prescHdObj.getPrescriptionDate());

                response.setPatientPrescriptionHd(hd);
            }

            // ---------------- PRESCRIPTION DT ----------------
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

                Optional<MasStoreItem> itemOpt = masStoreItemRepository.findById(dt.getItemId());

                itemOpt.ifPresent(item -> {
                    newDt.setItemName(item.getNomenclature());
                    newDt.setAdispQty(item.getAdispQty());

                    if (item.getDispUnit() != null) {
                        newDt.setDispUnit(item.getDispUnit().getUnitName());
                        newDt.setDepUnit(item.getDispUnit().getUnitName());
                    }

                    if (item.getItemClassId() != null) {
                        newDt.setItemClassId(item.getItemClassId().getItemClassId());
                    }
                });

                Long stocks = 0L;

                if (hospitalId != null && dt.getItemId() != null) {
                    Long stockVal = stockFound.getAvailableStocks(hospitalId, deptIdStore, dt.getItemId(), hospDefinedDays);
                    stocks = stockVal != null ? stockVal : 0L;
                }

                newDt.setStocks(stocks);

                newDtList.add(newDt);
            }

            response.setPatientPrescriptionDts(newDtList);

            // ---------------- FOLLOW UP / REFERRAL / ADMISSION ----------------
            if (opdPatientObj != null) {

                // Follow up
                response.setFollowUpFlag(opdPatientObj.getFollowUpFlag());
                if (isYes(opdPatientObj.getFollowUpFlag())) {
                    response.setFollowUpDays(opdPatientObj.getFollowUpDays());
                    response.setFollowUpDate(opdPatientObj.getFollowUpDate());
                }

                // Referral
                response.setReferralFlag(opdPatientObj.getReferralFlag());
                if (isYes(opdPatientObj.getReferralFlag())) {
                    response.setReferralRemarks(opdPatientObj.getReferralRemarks());
                    response.setReferralDate(opdPatientObj.getReferralDate());
                }

                // Admission
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

                    response.setVacantBed(0);
                    response.setOccupiedBed(0);
                }
            }

            responseList.add(response);
        }

        return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {
        });
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

    private List<OpdPatientRecallResponce.NewDgOrderHd> buildDgOrderHdList(List<DgOrderHd> hdList) {

        List<OpdPatientRecallResponce.NewDgOrderHd> newHdList = new ArrayList<>();

        for (DgOrderHd hdObj : safeList(hdList)) {

            if (hdObj == null) continue;

            OpdPatientRecallResponce.NewDgOrderHd hd = new OpdPatientRecallResponce.NewDgOrderHd();

            hd.setDgOrderHdId(hdObj.getId());
            hd.setOrderDate(hdObj.getOrderDate());
            hd.setOrderNo(hdObj.getOrderNo());
            hd.setOrderStatus(hdObj.getOrderStatus());
            hd.setCollectionStatus(hdObj.getCollectionStatus());
            hd.setPaymentStatus(hdObj.getPaymentStatus());
            hd.setAppointmentDate(hdObj.getAppointmentDate());

            List<DgOrderDt> dtList = safeList(dgOrderDtRepo.findByOrderhdId(hdObj));

            List<OpdPatientRecallResponce.NewDgOrderDt> newDtList = new ArrayList<>();

            for (DgOrderDt dt : dtList) {

                if (dt == null) continue;

                OpdPatientRecallResponce.NewDgOrderDt nd = new OpdPatientRecallResponce.NewDgOrderDt();

                nd.setDgOrderDtId(dt.getId());
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

            hd.setDgOrderDts(newDtList);
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

            Page<PatientWaitingListProjection> projectionPage = visitRepository.findWaitingPatientsByHospitalWithFilters(hospitalId, departmentId, AppConstants.STATUS_Y.toLowerCase(), AppConstants.STATUS_Y.toLowerCase(), AppConstants.STATUS_N.toLowerCase(), patientName, mobileNumber, doctorId, sessionId, pageable);

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
    public ApiResponse<Page<PreviousOpdVisitResponse>> getPreviousOpdVisit(
            Long patientId, Long hospitalId, int page, int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("visitDate").descending());
            Page<PreviousOpdVisitProjection> projectionPage = visitRepository.getPreviousOpdVisit(patientId, hospitalId, pageable);

            //Projection → DTO
            Page<PreviousOpdVisitResponse> responsePage =
                    projectionPage.map(p -> {
                        PreviousOpdVisitResponse res = new PreviousOpdVisitResponse();
                        res.setVisitDate(p.getVisitDate());
                        res.setDoctorName(p.getDoctorName());
                        res.setDepartment(p.getDepartment());
                        res.setIcdDiag(p.getIcdDiag());
                        res.setWorkingDiag(p.getWorkingDiag());
                        return res;
                    });

            return ResponseUtils.createSuccessResponse(responsePage, new TypeReference<Page<PreviousOpdVisitResponse>>() {
            });

        } catch (Exception ex) {
            log.error("Error fetching getPriviousHistoryByPatient: ",ex);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
        }
    }

    @Override
    public ApiResponse<Page<PreviousOpdVitalsDetailsResponse>> getPreviousOpdVitalsDetailsHistory(Long patientId, Long hospitalId, int page, int size) {

        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("visitDate").descending());
            Page<PreviousOpdVitalsDetailsProjection> projectionPage = visitRepository.getPriviousOpdVitalsDetails(patientId, hospitalId, pageable);

            //Projection → DTO
            Page<PreviousOpdVitalsDetailsResponse> responsePage =
                    projectionPage.map(p -> {
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
            log.error("Error fetching getPriviousHistoryByPatient: ",ex);
            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {},
                    AppConstants.INTERNAL_SERVER_ERR_MSG,
                    HttpStatus.INTERNAL_SERVER_ERROR.value()
            );
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

//    @Override
//    @Transactional
//    public ApiResponse<OpdPatientDetailResponseDTO> createOpdPatientDetailWithBilling(OpdPatientDetailFinalRequest request) {
//        log.info("Creating OPD patient detail with comprehensive billing structure");
//        log.info("Creating OPD Patient Detail...");
//        ApiResponse<OpdPatientDetailResponseDTO> opdDetailResponse = createOpdPatientDetail(request);
//
//        if (opdDetailResponse == null) {
//            log.error("Failed to create OPD patient detail");
//            return ResponseUtils.createFailureResponse(null, new TypeReference<>() {
//            }, "Failed to create OPD patient detail", 400);
//        }
//
//        OpdPatientDetailResponseDTO opdResponse = opdDetailResponse.getResponse();
//        User currentUser = authUtil.getCurrentUser();
//        Patient patient = patientRepository.findById(request.getPatientId()).orElseThrow(() -> new SDDException(400,"Patient not found"));
//        Visit visit = visitRepository.findById(request.getVisitId()).orElseThrow(() -> new SDDException(400,"Visit not found"));
//
//        log.info("Creating Order Header for OPD...");
//        String orderNum = createOrderNum();
//        DgOrderHd orderHd = new DgOrderHd();
//        orderHd.setOrderNo(orderNum);
//        orderHd.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
//        orderHd.setCollectionStatus(AppConstants.STATUS_N.toLowerCase());
//        orderHd.setPaymentStatus(AppConstants.STATUS_N.toLowerCase());
//        orderHd.setCreatedBy(currentUser.getFullName());
//        orderHd.setHospitalId(currentUser.getHospital().getId());
//        orderHd.setPatientId(patient);
//        orderHd.setVisitId(visit);
//        orderHd.setDepartmentId(authUtil.getCurrentDepartmentId());
//        orderHd.setLastChgBy(currentUser.getFirstName() + " " + currentUser.getLastName());
//        orderHd.setCreatedOn(LocalDate.now());
//        orderHd.setLastChgDate(LocalDate.now());
//        orderHd.setLastChgTime(LocalTime.now().toString());
//
//        DgOrderHd savedOrderHd = dgOrderHdRepo.save(orderHd);
//        log.info("Order Header created successfully - Order ID: {}", savedOrderHd.getId());
//
//        log.info("Creating Billing Header...");
//        BigDecimal totalAmount = BigDecimal.ZERO;
//        BigDecimal discountAmount = BigDecimal.ZERO;
//        BigDecimal taxAmount = BigDecimal.ZERO;
//
//        if (request.getInvestigation() != null && !request.getInvestigation().isEmpty()) {
//            for (OpdPatientDetailFinalRequest.Investigation inv : request.getInvestigation()) {
//                DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(inv.getId()).orElseThrow(() -> new RuntimeException("Investigation not found with ID: " + inv.getId()));
//
//                BigDecimal amount = getInvestigationPrice(invEntity);
//                totalAmount = totalAmount.add(amount);
//            }
//        }
//
//        BillingHeader billingHeader = new BillingHeader();
//        String billNo = randomNumGenerator.generateOrderNumber("OPD", true, true);
//        billingHeader.setBillNo(billNo);
//        billingHeader.setPatient(patient);
//        billingHeader.setVisit(visit);
//        billingHeader.setPatientDisplayName(patient.getPatientFn());
//        billingHeader.setPatientAge(ageCalculator(patient.getPatientDob()));
//        billingHeader.setPatientGender(patient.getPatientGender() != null ? patient.getPatientGender().getGenderName() : "");
//        billingHeader.setPatientAddress(patient.getPatientAddress1());
//        billingHeader.setHospital(currentUser.getHospital());
//        billingHeader.setHospitalName(currentUser.getHospital().getHospitalName());
//        billingHeader.setHospitalAddress(currentUser.getHospital().getAddress());
//        billingHeader.setHospitalMobileNo(currentUser.getHospital().getContactNumber());
//        billingHeader.setHospitalGstin(currentUser.getHospital().getGstnNo());
//        billingHeader.setReferredBy(visit.getDoctorName());
//        billingHeader.setBillingDate(Instant.now());
//        billingHeader.setPaymentStatus(AppConstants.STATUS_N.toLowerCase());
//        billingHeader.setVisit(visit);
//        billingHeader.setHdorder(savedOrderHd);
//        billingHeader.setTotalAmount(totalAmount);
//        billingHeader.setDiscountAmount(discountAmount);
//        billingHeader.setNetAmount(totalAmount.subtract(discountAmount).add(taxAmount));
//        billingHeader.setTaxTotal(taxAmount);
//        billingHeader.setCreatedBy(currentUser.getFirstName() + " " + currentUser.getLastName());
//        billingHeader.setCreatedDt(Instant.now());
//        billingHeader.setUpdatedDt(Instant.now());
//        billingHeader.setBillDate(OffsetDateTime.now());
//        billingHeader.setUpdatedAt(OffsetDateTime.now());
//
//        BillingHeader savedBillingHeader = billingHeaderRepository.save(billingHeader);
//        log.info("Billing Header created successfully - Bill ID: {}", savedBillingHeader.getId());
//
//        log.info("Creating Order and Billing Details...");
//        if (request.getInvestigation() != null && !request.getInvestigation().isEmpty()) {
//            for (OpdPatientDetailFinalRequest.Investigation investigation : request.getInvestigation()) {
//                DgOrderDt orderDt = new DgOrderDt();
//                orderDt.setOrderhdId(savedOrderHd);
//                orderDt.setOrderStatus(AppConstants.STATUS_N.toLowerCase());
//                orderDt.setBillingStatus(AppConstants.STATUS_N.toLowerCase());
//                orderDt.setOrderQty(1);
//                orderDt.setCreatedBy(currentUser.getFullName());
//                orderDt.setLastChgBy(currentUser.getFullName());
//                orderDt.setLastChgDate(LocalDate.now());
//                orderDt.setLastChgTime(LocalTime.now().toString());
//                orderDt.setCreatedon(Instant.now());
//                orderDt.setBillingHd(savedBillingHeader);
//
//                DgOrderDt savedOrderDt = dgOrderDtRepo.save(orderDt);
//                log.info("Order Detail created - Detail ID: {}", savedOrderDt.getId());
//
//                BillingDetail billingDetail = new BillingDetail();
//                billingDetail.setBillingHd(savedBillingHeader);
//                billingDetail.setBillHd(savedBillingHeader);
//                billingDetail.setItemName(investigation.getInvestigationName());
//                billingDetail.setQuantity(1);
//                billingDetail.setCreatedDt(OffsetDateTime.now());
//                billingDetail.setUpdatedDt(OffsetDateTime.now());
//                billingDetail.setCreatedAt(Instant.now());
//
//                DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(investigation.getId()).orElseThrow(() -> new RuntimeException("Investigation not found with ID: " + investigation.getId()));
//
//                BigDecimal amount = getInvestigationPrice(invEntity);
//
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
//            }
//        }
//
//        log.info("Successfully created OPD patient detail with billing - Order Number: {}, Bill No: {}", orderNum, billNo);
//
//        opdResponse.setOrderId(Long.valueOf(savedOrderHd.getId()));
//        opdResponse.setBillingHeaderId(savedBillingHeader.getId());
//
//        return ResponseUtils.createSuccessResponse(opdResponse, new TypeReference<>() {
//        });
//
//    }


    @Override
    public ApiResponse<List<PrescriptionDetailResponse>> getPrescriptionDetailsByPatientId(Long patientId) {
        try {
            log.info("Fetching prescription details for patient ID: {}", patientId);

            if (patientId == null || patientId <= 0) {
                log.warn("Invalid patient ID: {}", patientId);
                return ResponseUtils.createFailureResponse(new ArrayList<>(), new TypeReference<>() {
                }, "Patient ID is invalid", 400);
            }
            List<PrescriptionDetailProjection> prescriptionDetails = prescriptionDtRepository.findPrescriptionDetailsByPatientIdWithinLimitedDays(patientId,prescriptionHistoryDays);
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
        return PrescriptionDetailResponse.builder()
                .prescriptionDtId(projection.getPrescriptionDtId())
                .prescriptionHdId(projection.getPrescriptionHdId())
                .itemId(projection.getItemId()).itemName("")
                .dosage(projection.getDosage())
                .frequency(projection.getFrequency())
                .days(projection.getDays())
                .total(projection.getTotal())
                .issuedQty(projection.getIssuedQty())
                .route(projection.getRoute())
                .instruction(projection.getInstruction())
                .unitPrice(projection.getUnitPrice())
                .discount(projection.getDiscount())
                .gstRate(projection.getGstRate())
                .lineCost(projection.getLineCost())
                .status(projection.getStatus())
                .batchNo(projection.getBatchNo())
                .expiryDate(projection.getExpiryDate())
                .itemName(projection.getItemName())
                .build();
    }
}

