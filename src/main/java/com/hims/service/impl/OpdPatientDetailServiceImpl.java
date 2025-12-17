package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.exception.RecordNotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

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

    private final StoreItemBatchStockRepository storeItemBatchStockRepository;
    private final LabDtRepository dgOrderDtRepo;

    private final PatientPrescriptionHdRepository patientPrescriptionHdRepository;

    private final PatientPrescriptionDtRepository patientPrescriptionDtRepository;

    private final RandomNumGenerator randomNumGenerator;

    private final MasFrequencyRepository masFrequencyRepository;
    private final AuthUtil authUtil;

    private final MasStoreItemRepository masStoreItemRepository;

    private final ProcedureHeaderRepository procedureHeaderRepository;

    private final ProcedureDetailsRepository procedureDetailsRepository;

    private  final MasProcedureRepository masProcedureRepository;

    private final MasCareLevelRepo masCareLevelRepository;

    private final MasWardCategoryRepository masWardCategoryRepository;

    private final MasDepartmentRepository masDepartmentRepository;

    @Value("${hos.define.storeDay}")
    private Integer hospDefinedDays;

    @Value("${hos.define.storeId}")
    private Integer deptIdStore;

    public String createOrderNum() {
        return randomNumGenerator.generateOrderNumber("OPD",true,true);
    }


    @Override
    public ApiResponse<OpdPatientVitalResponce> getOpdPatientByVisit(Long visitId) {

        if (visitId == null) {
            throw new IllegalArgumentException("Visit ID must not be null");
        }

        OpdPatientDetail opdPObj = opdPatientDetailRepository.findByVisitId(visitId);

        if (opdPObj == null) {
            return ResponseUtils.createNotFoundResponse("OPD details not found for visitId: " + visitId, 404);
        }

        OpdPatientVitalResponce responseDto = mapToVitalResponse(opdPObj);

        return ResponseUtils.createSuccessResponse(responseDto, new TypeReference<>() {});
    }

    private OpdPatientVitalResponce mapToVitalResponse(OpdPatientDetail opd) {

        OpdPatientVitalResponce res = new OpdPatientVitalResponce();

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
    public ApiResponse<OpdPatientDetail> createOpdPatientDetail(
            OpdPatientDetailFinalRequest request) {

        // ===================== BASIC VALIDATION =====================
        if (request == null) {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "Request body cannot be null", 400);
        }

        log.info("Starting createOpdPatientDetail process...");
        log.info("Request Data: {}", request);

        Long deptId = authUtil.getCurrentDepartmentId();
        User useObj = authUtil.getCurrentUser();

        if (useObj == null || useObj.getHospital() == null) {
            throw new RuntimeException("Authenticated user or hospital not found");
        }

        // ===================== CREATE OR UPDATE =====================
        OpdPatientDetail opdPatientDetail;

        if (request.getOpdPatientDetailId() == null) {
            opdPatientDetail = new OpdPatientDetail();
            log.info("Creating new OpdPatientDetail...");
        } else {
            opdPatientDetail = opdPatientDetailRepository
                    .findById(request.getOpdPatientDetailId())
                    .orElseThrow(() -> new RuntimeException(
                            "OpdPatientDetail not found with ID: "
                                    + request.getOpdPatientDetailId()));
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
        if ((request.getWorkingDiag() == null || request.getWorkingDiag().isBlank()) &&
                (request.getIcdDiag() == null || request.getIcdDiag().isEmpty())) {

            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "One is mandatory: Working Diagnosis or ICD Diagnosis", 400);
        }

        opdPatientDetail.setWorkingDiag(request.getWorkingDiag());

        if (request.getIcdDiag() != null && !request.getIcdDiag().isEmpty()) {
            String joinedNames = request.getIcdDiag().stream()
                    .filter(Objects::nonNull)
                    .map(OpdPatientDetailFinalRequest.IcdDiagnosis::getIcdDiagName)
                    .filter(Objects::nonNull)
                    .collect(Collectors.joining(","));
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

            if (request.getInvestigation().stream()
                    .anyMatch(i -> i == null || i.getInvestigationDate() == null)) {
                throw new RuntimeException("Investigation date cannot be null");
            }

            opdPatientDetail.setLabFlag(request.getLabFlag());
            opdPatientDetail.setRadioFlag(request.getRadioFlag());

            String orderNumOPD = createOrderNum();

            Map<LocalDate, List<OpdPatientDetailFinalRequest.Investigation>> grouped =
                    request.getInvestigation().stream()
                            .collect(Collectors.groupingBy(
                                    OpdPatientDetailFinalRequest.Investigation::getInvestigationDate));

            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

            Visit visit = visitRepository.findById(request.getVisitId())
                    .orElseThrow(() -> new RuntimeException("Visit not found"));

            for (Map.Entry<LocalDate, List<OpdPatientDetailFinalRequest.Investigation>> entry : grouped.entrySet()) {

                DgOrderHd dgOrderHd = new DgOrderHd();
                dgOrderHd.setOrderDate(LocalDate.now());
                dgOrderHd.setAppointmentDate(entry.getKey());
                dgOrderHd.setOrderNo(orderNumOPD);
                dgOrderHd.setOrderStatus("n");
                dgOrderHd.setCollectionStatus("n");
                dgOrderHd.setPaymentStatus(
                        "y".equalsIgnoreCase(useObj.getHospital().getLabBilling()) ? "n" : "y");

                dgOrderHd.setSource("OPD PATIENT");
                dgOrderHd.setDiscountId(1);
                dgOrderHd.setPatientId(patient);
                dgOrderHd.setDepartmentId(Math.toIntExact(request.getDepartmentId()));
                dgOrderHd.setHospitalId(Math.toIntExact(request.getHospitalId()));
                dgOrderHd.setVisitId(visit);
                dgOrderHd.setCreatedBy(useObj.getFirstName());
                dgOrderHd.setLastChgBy(useObj.getFirstName());
                dgOrderHd.setCreatedOn(LocalDate.now());
                dgOrderHd.setLastChgDate(LocalDate.now());
                dgOrderHd.setLastChgTime(LocalTime.now().toString());

                dgOrderHd = dgOrderHdRepo.save(dgOrderHd);

                for (OpdPatientDetailFinalRequest.Investigation invObj : entry.getValue()) {

                    if (invObj == null || invObj.getId() == null) {
                        throw new RuntimeException("Investigation ID is missing");
                    }

                    DgMasInvestigation invEntity =
                            dgMasInvestigationRepository.findById(invObj.getId())
                                    .orElseThrow(() -> new RuntimeException(
                                            "Investigation not found with ID: " + invObj.getId()));

                    if (invEntity.getMainChargeCodeId() == null ||
                            invEntity.getSubChargeCodeId() == null) {
                        throw new RuntimeException("Charge codes not configured");
                    }

                    DgOrderDt dgOrderDt = new DgOrderDt();
                    dgOrderDt.setInvestigationId(invEntity);
                    dgOrderDt.setOrderhdId(dgOrderHd);
                    dgOrderDt.setAppointmentDate(invObj.getInvestigationDate());
                    dgOrderDt.setOrderQty(1);
                    dgOrderDt.setOrderStatus("n");
                    dgOrderDt.setBillingStatus(dgOrderHd.getPaymentStatus());
                    dgOrderDt.setCreatedBy(useObj.getFirstName());
                    dgOrderDt.setLastChgBy(useObj.getFirstName());
                    dgOrderDt.setCreatedon(Instant.now());
                    dgOrderDt.setLastChgDate(LocalDate.now());
                    dgOrderDt.setLastChgTime(LocalTime.now().toString());
                    dgOrderDt.setMainChargecodeId(
                            invEntity.getMainChargeCodeId().getChargecodeId());
                    dgOrderDt.setSubChargeid(
                            invEntity.getSubChargeCodeId().getSubId());

                    dgOrderDtRepo.save(dgOrderDt);
                }
            }
        }

        // ======================== TREATMENT ========================
        opdPatientDetail.setTreatmentAdvice(request.getTreatmentAdvice());

        if (request.getTreatment() != null && !request.getTreatment().isEmpty()) {

            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

            PatientPrescriptionHd hd = new PatientPrescriptionHd();
            hd.setHospitalId(useObj.getHospital().getId());
            hd.setPatientId(patient.getId());
            hd.setDepartmentId(deptId);
            hd.setDoctorName(useObj.getFirstName());
            hd.setPrescriptionDate(LocalDateTime.now());
            hd.setStatus("n");
            hd.setBillingStatus(
                    "y".equalsIgnoreCase(useObj.getHospital().getMedicineBilling()) ? "n" : "y");
            hd.setCreatedBy(useObj.getFirstName());
            hd.setTotalCost(BigDecimal.ZERO);
            hd.setTotalGst(BigDecimal.ZERO);
            hd.setTotalDiscount(BigDecimal.ZERO);
            hd.setNetAmount(BigDecimal.ZERO);

            PatientPrescriptionHd savedHd =
                    patientPrescriptionHdRepository.save(hd);

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
                dt.setStatus("n");

                patientPrescriptionDtRepository.save(dt);
            }
        }

        // ====================== GENERAL DETAILS =====================
        opdPatientDetail.setOpdDate(Instant.now());
        opdPatientDetail.setPatient(patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found")));
        opdPatientDetail.setVisit(visitRepository.findById(request.getVisitId())
                .orElseThrow(() -> new RuntimeException("Visit not found")));
        opdPatientDetail.setDepartment(departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found")));
        opdPatientDetail.setHospital(hospitalRepository
                .findById(useObj.getHospital().getId())
                .orElseThrow(() -> new RuntimeException("Hospital not found")));
        opdPatientDetail.setDoctor(userRepository.findById(useObj.getUserId())
                .orElseThrow(() -> new RuntimeException("Doctor not found")));

        opdPatientDetail.setLastChgBy(useObj.getUsername());
        opdPatientDetail.setLastChgDate(Instant.now());

        // ========================= Admission Advice =====================================

        if (isYes(request.getAdmissionFlag())) {

            masCareLevelRepository.findById(request.getAdmissionCareLevel())
                    .ifPresent(opdPatientDetail::setAdmissionCareLevel);

            masWardCategoryRepository.findById(request.getAdmissionWardCategory())
                    .ifPresent(opdPatientDetail::setAdmissionWardCategory);

            masDepartmentRepository.findById(request.getAdmissionWard())
                    .ifPresent(opdPatientDetail::setAdmissionWard);

            opdPatientDetail.setAdmissionFlag("y");
            opdPatientDetail.setAdmissionAdvisedDate(request.getAdmissionAdvisedDate());
            opdPatientDetail.setAdmissionRemarks(request.getAdmissionRemarks());
            opdPatientDetail.setAdmissionPriority(request.getAdmissionPriority());
        } else {
            opdPatientDetail.setAdmissionFlag("n");
        }


        // ========================= Follow up =========================

        if (isYes(request.getFollowUpFlag())) {
            opdPatientDetail.setFollowUpFlag("y");
            opdPatientDetail.setFollowUpDays(request.getFollowUpDays());
            opdPatientDetail.setFollowUpDate(request.getFollowUpDate());
        } else {
            opdPatientDetail.setFollowUpFlag("n");
        }


        //  =========================== referral ==============================
        opdPatientDetail.setReferralFlag(
                isYes(request.getReferralFlag()) ? "y" : "n"
        );


        // ====================== SAVE OPD ============================
        OpdPatientDetail saved =
                opdPatientDetailRepository.save(opdPatientDetail);

        // ====================== ICD SAVE (NO DUPLICATES) ============
        if (request.getIcdDiag() != null && !request.getIcdDiag().isEmpty()) {

            dischargeIcdCodeRepository
                    .deleteByOpdPatientDetailsId(saved.getOpdPatientDetailsId());

            for (OpdPatientDetailFinalRequest.IcdDiagnosis icd : request.getIcdDiag()) {
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
        Visit visit = visitRepository.findById(request.getVisitId())
                .orElseThrow(() -> new RuntimeException("Visit not found"));
        visit.setVisitStatus("c");
        visitRepository.save(visit);

        return ResponseUtils.createSuccessResponse(saved, new TypeReference<>() {});
    }

    @Transactional
    @Override
    public ApiResponse<OpdPatientDetail> recallOpdPatientDetail(
            RecallOpdPatientDetailRequest request) {

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
        OpdPatientDetail opdObj = opdPatientDetailRepository.findById(request.getOpdPatientId())
                .orElseThrow(() ->
                        new RuntimeException("OPD record not found: " + request.getOpdPatientId()));

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

            if (request.getAdmissionCareLevel() != null) {
                masCareLevelRepository.findById(request.getAdmissionCareLevel())
                        .ifPresent(opdObj::setAdmissionCareLevel);
            }
            if (request.getAdmissionWardCategory() != null) {
                masWardCategoryRepository.findById(request.getAdmissionWardCategory())
                        .ifPresent(opdObj::setAdmissionWardCategory);
            }
            if (request.getAdmissionWard() != null) {
                masDepartmentRepository.findById(request.getAdmissionWard())
                        .ifPresent(opdObj::setAdmissionWard);
            }

            opdObj.setAdmissionFlag("y");
            opdObj.setAdmissionAdvisedDate(request.getAdmissionAdvisedDate());
            opdObj.setAdmissionRemarks(request.getAdmissionRemarks());
            opdObj.setAdmissionPriority(request.getAdmissionPriority());
        } else {
            opdObj.setAdmissionFlag("n");
        }

        // ===================== FOLLOW UP =====================
        if (isYes(request.getFollowUpFlag())) {
            opdObj.setFollowUpFlag("y");
            opdObj.setFollowUpDays(request.getFollowUpDays());
            opdObj.setFollowUpDate(request.getFollowUpDate());
        } else {
            opdObj.setFollowUpFlag("n");
        }

        opdObj.setReferralFlag(isYes(request.getReferralFlag()) ? "y" : "n");

        OpdPatientDetail saved = opdPatientDetailRepository.save(opdObj);

        // ===================== INVESTIGATIONS =====================
        List<RecallOpdPatientDetailRequest.InvestigationRequest> invList = request.getInvestigations();

        if (invList != null && !invList.isEmpty()) {

            String orderNumOPD = createOrderNum();

            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));

            Visit visit = visitRepository.findById(request.getVisitId())
                    .orElseThrow(() -> new RuntimeException("Visit not found"));

            Map<LocalDate, DgOrderHd> existingHdByDate = new HashMap<>();

            List<DgOrderHd> existingHdList = dgOrderHdRepo.findAllByVisitId(visit);
            if (existingHdList != null) {
                for (DgOrderHd hd : existingHdList) {
                    if (hd != null && hd.getAppointmentDate() != null) {
                        existingHdByDate.put(hd.getAppointmentDate(), hd);
                    }
                }
            }

            Map<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> grouped =
                    invList.stream()
                            .filter(Objects::nonNull)
                            .filter(i -> i.getId() == null)
                            .filter(i -> i.getDate() != null)
                            .collect(Collectors.groupingBy(
                                    RecallOpdPatientDetailRequest.InvestigationRequest::getDate));

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
                    hd.setDepartmentId(Math.toIntExact(request.getDepartmentId()));
                    hd.setHospitalId(Math.toIntExact(request.getHospitalId()));
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

                    DgMasInvestigation invEntity =
                            dgMasInvestigationRepository.findById(inv.getInvestigationId())
                                    .orElseThrow(() -> new RuntimeException(
                                            "Investigation not found: " + inv.getInvestigationId()));

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

            Optional<RecallOpdPatientDetailRequest.TreatmentRequest> existing =
                    treatments.stream().filter(t -> t != null && t.getTreatmentId() != null).findFirst();

            if (existing.isPresent()) {
                PatientPrescriptionDt dt =
                        patientPrescriptionDtRepository.findById(existing.get().getTreatmentId())
                                .orElseThrow(() -> new RuntimeException("Treatment not found"));
                prescriptionHdId = dt.getPrescriptionHdId();
            } else {

                patientPrescriptionHdRepository.findLatestByPatientId(request.getPatientId())
                        .ifPresent(hd -> {
                            patientPrescriptionDtRepository.deleteByPrescriptionHdId(hd.getPrescriptionHdId());
                            patientPrescriptionHdRepository.deleteById(hd.getPrescriptionHdId());
                        });

                Patient patient = patientRepository.findById(request.getPatientId())
                        .orElseThrow(() -> new RuntimeException("Patient not found"));

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

            treatments.stream()
                    .filter(t -> t != null && t.getTreatmentId() == null)
                    .forEach(trt -> {
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
                    DischargeIcdCode existing =
                            dischargeIcdCodeRepository.findById(icd.getId())
                                    .orElseThrow(() -> new RuntimeException("ICD not found"));
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


        return ResponseUtils.createSuccessResponse(saved, new TypeReference<>() {});
    }

    private boolean isYes(String flag) {
        return flag != null && flag.equalsIgnoreCase("y");
    }


    @Transactional
    public void deleteOrderDetails(List<Integer> removedOrderDtIds) {
        if (removedOrderDtIds == null || removedOrderDtIds.isEmpty()) return;

        for (Integer dtId : new ArrayList<>(removedOrderDtIds)) {
            if (dtId == null) continue;

            DgOrderDt orderDt = dgOrderDtRepo.findById(dtId)
                    .orElseThrow(() -> new RuntimeException("OrderDt not found: " + dtId));

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

            List<Integer> allDtIds = allOrderDts.stream()
                    .map(DgOrderDt::getId)
                    .toList();

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

            PatientPrescriptionDt dt = patientPrescriptionDtRepository.findById(dtId)
                    .orElseThrow(() -> new RuntimeException("PrescriptionDt not found: " + dtId));

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

            List<Long> allDtIds = allDtOfHd.stream()
                    .map(PatientPrescriptionDt::getPrescriptionDtId)
                    .toList();

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

                String fullName = buildFullName(
                        v.getPatient().getPatientFn(),
                        v.getPatient().getPatientMn(),
                        v.getPatient().getPatientLn()
                );
                res.setPatientName(fullName);

                res.setGender(
                        v.getPatient().getPatientGender() != null
                                ? v.getPatient().getPatientGender().getGenderName()
                                : null
                );

                res.setRelation(
                        v.getPatient().getPatientRelation() != null
                                ? v.getPatient().getPatientRelation().getRelationName()
                                : null
                );
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

                String docFullName = buildFullName(
                        v.getDoctor().getFirstName(),
                        v.getDoctor().getMiddleName(),
                        v.getDoctor().getLastName()
                );
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

        return ResponseUtils.createSuccessResponse(
                responseList,
                new TypeReference<List<OpdPatientDetailsWaitingresponce>>() {}
        );
    }

    @Override
    public ApiResponse<List<OpdPatientDetailsWaitingresponce>> getActiveVisitsWithFilters(ActiveVisitSearchRequest req) {

        LocalDate visitDate = req.getDate() != null
                ? req.getDate().atZone(ZoneId.systemDefault()).toLocalDate()
                : LocalDate.now();

        List<Visit> activeVisits = visitRepository.findActiveVisitsWithFilters(
                req.getDoctorId(),
                req.getSessionId(),
                req.getEmployeeNo(),
                req.getPatientName(),
                visitDate
        );

        List<OpdPatientDetailsWaitingresponce> responseList = new ArrayList<>();

        for (Visit v : activeVisits) {
            OpdPatientDetailsWaitingresponce res = new OpdPatientDetailsWaitingresponce();

            // Patient Info
            if (v.getPatient() != null) {
                res.setPatientId(v.getPatient().getId());
                res.setEmployeeNo(v.getPatient().getUhidNo());
                res.setMobileNo(v.getPatient().getPatientMobileNumber());
                res.setDob(v.getPatient().getPatientDob());
                res.setAge(v.getPatient().getPatientAge());
                res.setDisplayPatientStatus(v.getDisplayPatientStatus());
                res.setVisitDate(v.getVisitDate());
                res.setPatientName(
                        buildFullName(
                                v.getPatient().getPatientFn(),
                                v.getPatient().getPatientMn(),
                                v.getPatient().getPatientLn()
                        )
                );

                res.setGender(
                        v.getPatient().getPatientGender() != null
                                ? v.getPatient().getPatientGender().getGenderName()
                                : null
                );

                res.setRelation(
                        v.getPatient().getPatientRelation() != null
                                ? v.getPatient().getPatientRelation().getRelationName()
                                : null
                );
            }

            // Visit Info
            res.setVisitId(v.getId());
            res.setTokenNo(v.getTokenNo() != null ? String.valueOf(v.getTokenNo()) : null);

            if (v.getDepartment() != null) {
                res.setDeptId(v.getDepartment().getId());
                res.setDeptName(v.getDepartment().getDepartmentName());
            }

            if (v.getDoctor() != null) {
                res.setDocterId(v.getDoctor().getUserId());
                res.setDocterName(
                        buildFullName(
                                v.getDoctor().getFirstName(),
                                v.getDoctor().getMiddleName(),
                                v.getDoctor().getLastName()
                        )
                );
            }

            if (v.getHospital() != null) {
                res.setHospitalId(v.getHospital().getId());
            }

            if (v.getSession() != null) {
                res.setSessionId(v.getSession().getId());
                res.setSessionName(v.getSession().getSessionName());
            }

            responseList.add(res);
        }

        // Sort responseList by tokenNo numerically (ascending)
        responseList.sort(Comparator.comparingInt(res -> {
            try {
                return res.getTokenNo() != null ? Integer.parseInt(res.getTokenNo()) : Integer.MAX_VALUE;
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE; // invalid tokenNo goes to the end
            }
        }));

        return ResponseUtils.createSuccessResponse(
                responseList,
                new TypeReference<>() {}
        );
    }


//    Recall Api

    @Override
    public ApiResponse<List<OpdPatientRecallResponce>> getRecallVisit(String name, String mobile, LocalDate visitDate) {

        if (visitDate == null && isEmpty(mobile) && isEmpty(name)) {
            visitDate = LocalDate.now();
        }

        mobile = safeString(mobile);
        name = safeString(name);

        // ---------------- FETCH VISITS ----------------
        List<Visit> recallVisit = visitRepository.searchRecallVisits(visitDate, mobile, name);
        List<OpdPatientRecallResponce> responseList = new ArrayList<>();

        for (Visit visitObj : recallVisit) {

            if (visitObj == null || visitObj.getPatient() == null) continue;

            Patient patientObj = visitObj.getPatient();
            User docObj = visitObj.getDoctor();
            MasDepartment deptObj = visitObj.getDepartment();
            MasGender genderObj = patientObj.getPatientGender();
            MasRelation relationObj = patientObj.getPatientRelation();

            OpdPatientDetail opdPatientObj = opdPatientDetailRepository
                    .findByVisitId(visitObj.getId());

            List<DgOrderHd> dgOrderHdList = safeList(dgOrderHdRepo.findAllByVisitId(visitObj));

            PatientPrescriptionHd patientPrescHdObj =
                    patientPrescriptionHdRepository.findByPatientId(patientObj.getId());

            List<PatientPrescriptionDt> prescDtList =
                    patientPrescHdObj != null
                            ? safeList(patientPrescriptionDtRepository
                            .findByPrescriptionHdId(patientPrescHdObj.getPrescriptionHdId()))
                            : Collections.emptyList();

            OpdPatientRecallResponce response = new OpdPatientRecallResponce();

            // ---------------- BASIC PATIENT INFO ----------------
            response.setPatientName(buildFullName(
                    patientObj.getPatientFn(),
                    patientObj.getPatientMn(),
                    patientObj.getPatientLn()
            ));

            response.setMobileNo(patientObj.getPatientMobileNumber());
            response.setGender(genderObj != null ? genderObj.getGenderName() : null);
            response.setRelation(relationObj != null ? relationObj.getRelationName() : null);
            response.setDob(patientObj.getPatientDob());
            response.setAge(patientObj.getPatientAge());
            response.setDeptId(deptObj != null ? deptObj.getId() : null);
            response.setDeptName(deptObj != null ? deptObj.getDepartmentName() : null);
            response.setDocterId(docObj != null ? docObj.getUserId() : null);




            response.setHospitalId(
                    patientObj.getPatientHospital() != null
                            ? patientObj.getPatientHospital().getId()
                            : null
            );

            // Doctor name
            if (docObj != null) {
                response.setDocterName(buildFullName(
                        docObj.getFirstName(),
                        docObj.getMiddleName(),
                        docObj.getLastName()
                ));
            }

            response.setVisitId(visitObj.getId());
            response.setPatientId(patientObj.getId());

            // ------------------- OPD DETAILS --------------------
            if (opdPatientObj != null) {
                mapOpdDetails(response, opdPatientObj);
            }

            // ------------------- DG ORDER HD --------------------
            response.setDgOrderHdList(
                    buildDgOrderHdList(dgOrderHdList)
            );

            // ------------------- PRESCRIPTION HD --------------------
            if (patientPrescHdObj != null) {
                OpdPatientRecallResponce.NewDPatientPrescriptionHd newHd =
                        new OpdPatientRecallResponce.NewDPatientPrescriptionHd();

                newHd.setPrescriptionHdId(patientPrescHdObj.getPrescriptionHdId());
                newHd.setStatus(patientPrescHdObj.getStatus());
                newHd.setPrescriptionDate(patientPrescHdObj.getPrescriptionDate());

                response.setPatientPrescriptionHd(newHd);
            }

            response.setTreatmentAdvice(opdPatientObj.getTreatmentAdvice());

            // ------------------- PRESCRIPTION DT --------------------
            List<OpdPatientRecallResponce.NewDPatientPrescriptionDt> newPrescList =
                    new ArrayList<>();

            for (PatientPrescriptionDt dt : prescDtList) {

                OpdPatientRecallResponce.NewDPatientPrescriptionDt newDt =
                        new OpdPatientRecallResponce.NewDPatientPrescriptionDt();

                newDt.setPrescriptionDtId(dt.getPrescriptionDtId());
                newDt.setPrescriptionHdId(dt.getPrescriptionHdId());
                newDt.setStatus(dt.getStatus());
                newDt.setDosage(dt.getDosage());
                newDt.setFrequency(dt.getFrequency());
                newDt.setDays(dt.getDays());
                newDt.setTotal(dt.getTotal());
                newDt.setInstraction(dt.getInstruction());
                newDt.setItemId(dt.getItemId());

                // SAFE: Frequency
                MasFrequency freq = masFrequencyRepository.findByFrequencyName(dt.getFrequency());
                newDt.setFrequencyId(dt.getFrequency());

                // SAFE: Item
                masStoreItemRepository.findById(dt.getItemId()).ifPresent(item -> {
                    newDt.setDepUnit(item.getDispUnit() != null
                            ? item.getDispUnit().getUnitName() : null);
                    newDt.setItemName(item.getNomenclature());
                });

                // SAFE STOCK
                Long stocks = stockFound.getAvailableStocks(
                        authUtil.getCurrentUser().getHospital().getId(),
                        deptIdStore,
                        dt.getItemId(),
                        hospDefinedDays
                );
                newDt.setStocks(stocks);

                newPrescList.add(newDt);
            }



            // ----------- follow up ------------------------------
            response.setFollowUpFlag(opdPatientObj.getFollowUpFlag());
            response.setFollowUpDays(opdPatientObj.getFollowUpDays());
            response.setFollowUpDate(opdPatientObj.getFollowUpDate());

            // --------------- refferal -----------------------
            response.setReferralFlag(opdPatientObj.getReferralFlag());

            // ----------------- admission advice ----------------------
            response.setAdmissionFlag(opdPatientObj.getAdmissionFlag());
            response.setAdmissionRemarks(opdPatientObj.getAdmissionRemarks());
            response.setAdmissionAdvisedDate(opdPatientObj.getAdmissionAdvisedDate());
            response.setAdmissionCareLevel(opdPatientObj.getAdmissionCareLevel().getCareId());
            response.setAdmissionCareLevelName(opdPatientObj.getAdmissionCareLevel().getCareLevelName());
            response.setAdmissionWardCategory(opdPatientObj.getAdmissionWardCategory().getId());
            response.setAdmissionWardCategoryName(opdPatientObj.getAdmissionWardCategory().getCategoryName());
            response.setAdmissionWard(opdPatientObj.getAdmissionWard().getId());
            response.setAdmissionWardName(opdPatientObj.getAdmissionWard().getDepartmentName());
            response.setAdmissionPriority(opdPatientObj.getAdmissionPriority());
            response.setVacantBed(0);
            response.setOccupiedBed(0);


            response.setPatientPrescriptionDts(newPrescList);

            responseList.add(response);
        }

        return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});
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

        // ================= FINAL MEDICAL ADVICE =================
        response.setDoctorRemarks(opd.getFinalMedicalAdvice());

        // ================= ADMISSION =================
        response.setAdmissionFlag(opd.getAdmissionFlag());
        response.setAdmissionAdvisedDate(opd.getAdmissionAdvisedDate());
        response.setAdmissionRemarks(opd.getAdmissionRemarks());
        response.setAdmissionPriority(opd.getAdmissionPriority());

        if (opd.getAdmissionCareLevel() != null) {
            response.setAdmissionCareLevel(opd.getAdmissionCareLevel().getCareId());
            response.setAdmissionCareLevelName(
                    opd.getAdmissionCareLevel().getCareLevelName());
        }

        if (opd.getAdmissionWardCategory() != null) {
            response.setAdmissionWardCategory(opd.getAdmissionWardCategory().getId());
            response.setAdmissionWardCategoryName(
                    opd.getAdmissionWardCategory().getCategoryName());
        }

        if (opd.getAdmissionWard() != null) {
            response.setAdmissionWard(opd.getAdmissionWard().getId());
            response.setAdmissionWardName(
                    opd.getAdmissionWard().getDepartmentName());
        }

        // ================= FOLLOW UP =================
        response.setFollowUpFlag(opd.getFollowUpFlag());
        response.setFollowUpDate(opd.getFollowUpDate());
        response.setFollowUpDays(opd.getFollowUpDays());

        // ================= REFERRAL =================
        response.setReferralFlag(opd.getReferralFlag());


        // ICD LIST
        if (opd.getIcdDiag() != null && !opd.getIcdDiag().isEmpty()) {

            List<DischargeIcdCode> icdList = dischargeIcdCodeRepository
                    .findByOpdPatientDetailsIdAndVisitId(
                            opd.getOpdPatientDetailsId(),
                            opd.getVisit().getId()
                    );

            List<OpdPatientRecallResponce.IcdDiagnosis> newList = new ArrayList<>();

            for (DischargeIcdCode dis : icdList) {

                OpdPatientRecallResponce.IcdDiagnosis d =
                        new OpdPatientRecallResponce.IcdDiagnosis();

                d.setId(dis.getDischargeIcdCodeId());
                d.setIcdId(dis.getIcdId());

                String icdName = masIcdRepository.findById(dis.getIcdId())
                        .map(MasIcd::getIcdName)
                        .orElse(null);

                d.setIcdDiagName(icdName);

                newList.add(d);
            }

            response.setIcdDiag(newList);
        }

        response.setLabFlag(opd.getLabFlag());
        response.setRadioFlag(opd.getRadioFlag());
    }

    private List<OpdPatientRecallResponce.NewDgOrderHd> buildDgOrderHdList(List<DgOrderHd> hdList) {

        List<OpdPatientRecallResponce.NewDgOrderHd> newHdList = new ArrayList<>();

        for (DgOrderHd hdObj : safeList(hdList)) {

            OpdPatientRecallResponce.NewDgOrderHd hd =
                    new OpdPatientRecallResponce.NewDgOrderHd();

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

                OpdPatientRecallResponce.NewDgOrderDt nd =
                        new OpdPatientRecallResponce.NewDgOrderDt();

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
                nd.setPackageId(dt.getPackageId() != null
                        ? dt.getPackageId().getPackId()
                        : null);

                // Billing
                nd.setBillingHd(dt.getBillingHd() != null
                        ? dt.getBillingHd().getBillingHdId()
                        : null);

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

        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Visit not found with id: " + visitId));

        visit.setVisitStatus(status);

        visitRepository.save(visit);

        return ResponseUtils.createSuccessResponse(
                "Status updated successfully",
                new TypeReference<>() {}
        );
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
        Visit currentVisit = visitRepository.findById(visitId)
                .orElseThrow(() -> new RuntimeException("Visit not found"));

        // STEP 1 — Find previous CP visit
        Optional<Visit> cpVisitOpt = visitRepository.findCpVisit(
                doctorId, visitDate, "cp"
        );

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
        List<Visit> nextVisits = visitRepository.findNextVisits(
                doctorId, visitDate, currentVisit.getTokenNo()
        );

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
}

