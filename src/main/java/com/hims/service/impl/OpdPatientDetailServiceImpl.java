package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.ActiveVisitSearchRequest;
import com.hims.request.OpdPatientDetailFinalRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdPatientDetailsWaitingresponce;
import com.hims.response.OpdPatientRecallResponce;
import com.hims.response.RecallOpdPatientDetailRequest;
import com.hims.service.OpdPatientDetailService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import com.hims.utils.StockFound;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
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

    @Value("${hos.define.storeDay}")
    private Integer hospDefinedDays;

    @Value("${hos.define.storeId}")
    private Integer deptIdStore;

    public String createOrderNum() {
        return randomNumGenerator.generateOrderNumber("OPD",true,true);
    }


    @Override
    @Transactional
    public ApiResponse<OpdPatientDetail> createOpdPatientDetail(OpdPatientDetailFinalRequest request) {
        log.info("Starting createOpdPatientDetail process...");
        log.info("Request Data: {}", request);

        Long deptId = authUtil.getCurrentDepartmentId();
        User useObj = authUtil.getCurrentUser();

        OpdPatientDetail opdPatientDetail = new OpdPatientDetail();

        // ======================== VITAL DETAILS ====================
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

        // ======================== DIAGNOSIS =========================
        if ((request.getWorkingDiag() != null && !request.getWorkingDiag().isBlank()) ||
                (request.getIcdDiag() != null && !request.getIcdDiag().isEmpty())) {

            // Save Working Diagnosis
            opdPatientDetail.setWorkingDiag(request.getWorkingDiag());

            // Save ICD Names (comma-separated)
            if (request.getIcdDiag() != null && !request.getIcdDiag().isEmpty()) {

                String joinedNames = request.getIcdDiag()
                        .stream()
                        .map(OpdPatientDetailFinalRequest.IcdDiagnosis::getIcdDiagName)
                        .filter(Objects::nonNull)
                        .collect(Collectors.joining(","));

                opdPatientDetail.setIcdDiag(joinedNames);
            }
            else {
                opdPatientDetail.setIcdDiag(null);
            }

        } else {
            return ResponseUtils.createFailureResponse(
                    null, new TypeReference<>() {},
                    "One is mandatory: Working Diagnosis or ICD Diagnosis.", 400
            );
        }


        // ======================== CLINICAL HISTORY =================
        opdPatientDetail.setPastMedicalHistory(request.getPastMedicalHistory());
        opdPatientDetail.setFamilyHistory(request.getFamilyHistory());
        opdPatientDetail.setClinicalExamination(request.getClinicalExamination());
        opdPatientDetail.setPatientSignsSymptoms(request.getPatientSignsSymptoms());


        // ======================== INVESTIGATION ====================
        if (request.getInvestigation() != null && !request.getInvestigation().isEmpty()) {
            opdPatientDetail.setLabFlag(request.getLabFlag());
            opdPatientDetail.setRadioFlag(request.getRadioFlag());
            String orderNumOPD = createOrderNum();

            Map<LocalDate, List<OpdPatientDetailFinalRequest.Investigation>> groupedByDate =
                    request.getInvestigation().stream()
                            .collect(Collectors.groupingBy(OpdPatientDetailFinalRequest.Investigation::getInvestigationDate));

            for (Map.Entry<LocalDate, List<OpdPatientDetailFinalRequest.Investigation>> entry : groupedByDate.entrySet()) {
                LocalDate investigationDate = entry.getKey();
                List<OpdPatientDetailFinalRequest.Investigation> invList = entry.getValue();

                Patient patient = patientRepository.findById(request.getPatientId())
                        .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getPatientId()));

                Visit visit = visitRepository.findById(request.getVisitId())
                        .orElseThrow(() -> new RuntimeException("Visit not found with ID: " + request.getVisitId()));

                DgOrderHd dgOrderHd = new DgOrderHd();
                dgOrderHd.setOrderDate(LocalDate.now());
                dgOrderHd.setAppointmentDate(investigationDate);
                dgOrderHd.setOrderNo(orderNumOPD);
                dgOrderHd.setOrderStatus("n");
                dgOrderHd.setCollectionStatus("n");

                if ("y".equalsIgnoreCase(useObj.getHospital().getLabBilling())) {
                    dgOrderHd.setPaymentStatus("n");
                } else {
                    dgOrderHd.setPaymentStatus("y");
                }

                dgOrderHd.setSource("OPD PATIENT");
                dgOrderHd.setDiscountId(1);
                dgOrderHd.setPatientId(patient);
                dgOrderHd.setDepartmentId(Math.toIntExact(request.getDepartmentId()));
                dgOrderHd.setHospitalId(Math.toIntExact(request.getHospitalId()));
                dgOrderHd.setVisitId(visit);
                dgOrderHd.setLastChgBy(useObj.getFirstName() + " " + useObj.getLastName());
                dgOrderHd.setCreatedBy(useObj.getFirstName() + " " + useObj.getLastName());
                dgOrderHd.setCreatedOn(LocalDate.now());
                dgOrderHd.setLastChgDate(LocalDate.now());
                dgOrderHd.setLastChgTime(LocalTime.now().toString());

                dgOrderHd = dgOrderHdRepo.save(dgOrderHd);

                for (OpdPatientDetailFinalRequest.Investigation invObj : invList) {
                    DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(invObj.getId())
                            .orElseThrow(() -> new RuntimeException("Investigation not found with ID: " + invObj.getId()));

                    DgOrderDt dgOrderDt = new DgOrderDt();
                    dgOrderDt.setInvestigationId(invEntity);
                    dgOrderDt.setOrderhdId(dgOrderHd);
                    dgOrderDt.setAppointmentDate(invObj.getInvestigationDate());
                    dgOrderDt.setLastChgBy(useObj.getFirstName() + " " + useObj.getLastName());
                    dgOrderDt.setCreatedBy(useObj.getFirstName() + " " + useObj.getLastName());
                    dgOrderDt.setLastChgDate(LocalDate.now());
                    dgOrderDt.setBillingStatus(dgOrderHd.getPaymentStatus());
                    dgOrderDt.setOrderStatus("n");
                    dgOrderDt.setOrderQty(1);
                    dgOrderDt.setCreatedon(Instant.now());
                    dgOrderDt.setLastChgTime(LocalTime.now().toString());
                    dgOrderDt.setMainChargecodeId(invEntity.getMainChargeCodeId().getChargecodeId());
                    dgOrderDt.setSubChargeid(invEntity.getSubChargeCodeId().getSubId());

                    dgOrderDtRepo.save(dgOrderDt);
                }
            }
        }


        // ======================== TREATMENT =========================
        if (request.getTreatment() != null && !request.getTreatment().isEmpty()) {

            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getPatientId()));

            PatientPrescriptionHd precHdObj = new PatientPrescriptionHd();
            precHdObj.setHospitalId(useObj.getHospital().getId());
            precHdObj.setNisNo(null);
            precHdObj.setPatientId(patient.getId());
            precHdObj.setDepartmentId(deptId);
            precHdObj.setDoctorName(useObj.getFirstName() + " " + useObj.getLastName());
            precHdObj.setPrescriptionDate(LocalDateTime.now());
            precHdObj.setStatus("n");

            if ("y".equalsIgnoreCase(useObj.getHospital().getMedicineBilling())) {
                precHdObj.setBillingStatus("n");
            } else {
                precHdObj.setBillingStatus("y");
            }

            precHdObj.setCreatedBy(useObj.getFirstName() + " " + useObj.getLastName());
            precHdObj.setTotalCost(BigDecimal.ZERO);
            precHdObj.setTotalGst(BigDecimal.ZERO);
            precHdObj.setTotalDiscount(BigDecimal.ZERO);
            precHdObj.setNetAmount(BigDecimal.ZERO);

            PatientPrescriptionHd newPrescHd = patientPrescriptionHdRepository.save(precHdObj);

            for (OpdPatientDetailFinalRequest.Treatment trtObj : request.getTreatment()) {
                PatientPrescriptionDt precDtObj = new PatientPrescriptionDt();
                precDtObj.setPrescriptionHdId(newPrescHd.getPrescriptionHdId());
                precDtObj.setItemId(trtObj.getItemId());
                precDtObj.setDosage(trtObj.getDosage());
                precDtObj.setFrequency(trtObj.getFrequency());
                precDtObj.setDays(trtObj.getDays());
                precDtObj.setTotal(trtObj.getTotal());
                precDtObj.setInstruction(trtObj.getInstraction());
                precDtObj.setStatus("n");

                patientPrescriptionDtRepository.save(precDtObj);
            }
        }


        // ======================== GENERAL DETAILS ===================
        opdPatientDetail.setOpdDate(Instant.now());
        opdPatientDetail.setVaration(null);
        opdPatientDetail.setFollowUpFlag(null);
        opdPatientDetail.setFollowUpDays(null);
        opdPatientDetail.setTreatmentAdvice(null);
        opdPatientDetail.setSosFlag(null);
        opdPatientDetail.setRecmmdMedAdvice(null);
        opdPatientDetail.setMedicineFlag(null);
        opdPatientDetail.setReferralFlag(null);
        opdPatientDetail.setPoliceStation(null);
        opdPatientDetail.setPoliceName(null);

        opdPatientDetail.setPatient(patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getPatientId())));
        opdPatientDetail.setVisit(visitRepository.findById(request.getVisitId())
                .orElseThrow(() -> new RuntimeException("Visit not found with ID: " + request.getVisitId())));
        opdPatientDetail.setDepartment(departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found with ID: " + deptId)));
        opdPatientDetail.setHospital(hospitalRepository.findById(useObj.getHospital().getId())
                .orElseThrow(() -> new RuntimeException("Hospital not found with ID: " + useObj.getHospital().getId())));
        opdPatientDetail.setDoctor(userRepository.findById(useObj.getUserId())
                .orElseThrow(() -> new RuntimeException("Doctor not found with ID: " + useObj.getUserId())));

        opdPatientDetail.setLastChgDate(Instant.now());
        opdPatientDetail.setLastChgBy(useObj.getUsername());


        // ======================== SAVE OPD PATIENT DETAIL =====================
        OpdPatientDetail saved = opdPatientDetailRepository.save(opdPatientDetail);


        // ======================== SAVE ICD CODES (AFTER SAVING OPD) ============
        if (request.getIcdDiag() != null && !request.getIcdDiag().isEmpty()) {
            for (OpdPatientDetailFinalRequest.IcdDiagnosis reqObj : request.getIcdDiag()) {

                DischargeIcdCode icdObj = new DischargeIcdCode();
                icdObj.setIcdId(reqObj.getIcdId());
                icdObj.setOpdPatientDetailsId(saved.getOpdPatientDetailsId()); // FIXED
                icdObj.setVisitId(request.getVisitId());
                icdObj.setAddEditById(useObj.getUserId());
                icdObj.setAddEditDate(LocalDate.now());
                icdObj.setAddEditTime(LocalTime.now().toString());

                dischargeIcdCodeRepository.save(icdObj);
            }
        }


        // ======================== UPDATE VISIT STATUS =====================
        Visit visit = visitRepository.findById(request.getVisitId())
                .orElseThrow(() -> new RuntimeException("Visit not found with ID: " + request.getVisitId()));
        visit.setVisitStatus("c");
        visitRepository.save(visit);

        return ResponseUtils.createSuccessResponse(saved, new TypeReference<>() {});
    }

    @Transactional
    @Override
    public ApiResponse<OpdPatientDetail> recallOpdPatientDetail(RecallOpdPatientDetailRequest request) {
        User useObj = authUtil.getCurrentUser();

        Objects.requireNonNull(request, "Request cannot be null");
        Objects.requireNonNull(request.getOpdPatientId(), "OPD Patient ID is required");
        Objects.requireNonNull(request.getPatientId(), "Patient ID is required");
        Objects.requireNonNull(request.getVisitId(), "Visit ID is required");
        Objects.requireNonNull(request.getDepartmentId(), "Department ID is required");
        Objects.requireNonNull(request.getHospitalId(), "Hospital ID is required");

        OpdPatientDetail opdObj = opdPatientDetailRepository.findById(request.getOpdPatientId())
                .orElseThrow(() -> new RuntimeException("OPD record not found: " + request.getOpdPatientId()));

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

        OpdPatientDetail saved = opdPatientDetailRepository.save(opdObj);


        //  INVESTIGATION (DG ORDER)
        List<RecallOpdPatientDetailRequest.InvestigationRequest> investiObj = request.getInvestigations();

        if (investiObj != null && !investiObj.isEmpty()) {

            String orderNumOPD = createOrderNum();

            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getPatientId()));
            Visit visit = visitRepository.findById(request.getVisitId())
                    .orElseThrow(() -> new RuntimeException("Visit not found with ID: " + request.getVisitId()));

            List<DgOrderHd> existingHdList = dgOrderHdRepo.findAllByVisitId(visit);
            Map<LocalDate, DgOrderHd> existingHdByDate = new HashMap<>();
            if (existingHdList != null) {
                for (DgOrderHd hd : existingHdList) {
                    if (hd != null && hd.getAppointmentDate() != null) {
                        existingHdByDate.put(hd.getAppointmentDate(), hd);
                    }
                }
            }

            Map<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> groupedByDate =
                    investiObj.stream()
                            .filter(Objects::nonNull)
                            .filter(i -> i.getId() == null)
                            .filter(i -> i.getDate() != null)
                            .collect(Collectors.groupingBy(RecallOpdPatientDetailRequest.InvestigationRequest::getDate));

            for (Map.Entry<LocalDate, List<RecallOpdPatientDetailRequest.InvestigationRequest>> entry : groupedByDate.entrySet()) {

                LocalDate investigationDate = entry.getKey();
                List<RecallOpdPatientDetailRequest.InvestigationRequest> invList = entry.getValue();
                if (invList == null || invList.isEmpty()) continue;

                DgOrderHd dgOrderHd;

                if (existingHdByDate.containsKey(investigationDate)) {
                    dgOrderHd = existingHdByDate.get(investigationDate);
                } else {
                    dgOrderHd = new DgOrderHd();
                    dgOrderHd.setOrderDate(LocalDate.now());
                    dgOrderHd.setAppointmentDate(investigationDate);
                    dgOrderHd.setOrderNo(orderNumOPD);
                    dgOrderHd.setOrderStatus("n");
                    dgOrderHd.setCollectionStatus("n");
                    dgOrderHd.setPaymentStatus("n");
                    dgOrderHd.setSource("OPD PATIENT");
                    dgOrderHd.setDiscountId(1);
                    dgOrderHd.setPatientId(patient);
                    dgOrderHd.setDepartmentId(Math.toIntExact(request.getDepartmentId()));
                    dgOrderHd.setHospitalId(Math.toIntExact(request.getHospitalId()));
                    dgOrderHd.setVisitId(visit);
                    dgOrderHd.setLastChgBy(useObj.getFirstName() + " " + useObj.getLastName());
                    dgOrderHd.setCreatedBy(useObj.getFirstName() + " " + useObj.getLastName());
                    dgOrderHd.setCreatedOn(LocalDate.now());
                    dgOrderHd.setLastChgDate(LocalDate.now());
                    dgOrderHd.setLastChgTime(LocalTime.now().toString());

                    dgOrderHd = dgOrderHdRepo.save(dgOrderHd);
                    existingHdByDate.put(investigationDate, dgOrderHd);
                }

                for (RecallOpdPatientDetailRequest.InvestigationRequest invObj : invList) {
                    if (invObj == null || invObj.getInvestigationId() == null) continue;

                    DgMasInvestigation invEntity = dgMasInvestigationRepository.findById(invObj.getInvestigationId())
                            .orElseThrow(() -> new RuntimeException("Investigation not found with ID: " + invObj.getInvestigationId()));

                    DgOrderDt dgOrderDt = new DgOrderDt();
                    dgOrderDt.setOrderhdId(dgOrderHd);
                    dgOrderDt.setInvestigationId(invEntity);
                    dgOrderDt.setAppointmentDate(invObj.getDate());
                    dgOrderDt.setOrderQty(1);
                    dgOrderDt.setOrderStatus("n");
                    dgOrderDt.setBillingStatus("n");
                    dgOrderDt.setLastChgBy(useObj.getFirstName() + " " + useObj.getLastName());
                    dgOrderDt.setCreatedBy(useObj.getFirstName() + " " + useObj.getLastName());
                    dgOrderDt.setLastChgDate(LocalDate.now());
                    dgOrderDt.setCreatedon(Instant.now());
                    dgOrderDt.setLastChgTime(LocalTime.now().toString());

                    if (invEntity.getMainChargeCodeId() != null) {
                        dgOrderDt.setMainChargecodeId(invEntity.getMainChargeCodeId().getChargecodeId());
                    }
                    if (invEntity.getSubChargeCodeId() != null) {
                        dgOrderDt.setSubChargeid(invEntity.getSubChargeCodeId().getSubId());
                    }

                    dgOrderDtRepo.save(dgOrderDt);
                }
            }
        }


        //  TREATMENTS / PRESCRIPTIONS
        List<RecallOpdPatientDetailRequest.TreatmentRequest> treatmentObj = request.getTreatments();

        if (treatmentObj != null && !treatmentObj.isEmpty()) {

            boolean hasExisting = treatmentObj.stream()
                    .filter(Objects::nonNull)
                    .anyMatch(t -> t.getTreatmentId() != null);

            Long headerIdToUse = null;

            if (hasExisting) {
                Optional<RecallOpdPatientDetailRequest.TreatmentRequest> anyExistingOpt =
                        treatmentObj.stream().filter(Objects::nonNull).filter(t -> t.getTreatmentId() != null).findFirst();

                if (anyExistingOpt.isPresent()) {
                    Long existingTreatmentId = anyExistingOpt.get().getTreatmentId();
                    PatientPrescriptionDt existingDt = patientPrescriptionDtRepository.findById(existingTreatmentId)
                            .orElseThrow(() -> new RuntimeException("Treatment not found: " + existingTreatmentId));

                    headerIdToUse = existingDt.getPrescriptionHdId();
                } else {
                    throw new RuntimeException("Unexpected: treatment hasExisting true but no id found");
                }
            } else {
                Optional<PatientPrescriptionHd> oldHdOpt =
                        patientPrescriptionHdRepository.findLatestByPatientId(request.getPatientId());

                if (oldHdOpt.isPresent()) {
                    PatientPrescriptionHd oldHd = oldHdOpt.get();

                    patientPrescriptionDtRepository.deleteByPrescriptionHdId(oldHd.getPrescriptionHdId());

                    patientPrescriptionHdRepository.deleteById(oldHd.getPrescriptionHdId());
                }

                Patient patient = patientRepository.findById(request.getPatientId())
                        .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getPatientId()));

                PatientPrescriptionHd newHd = new PatientPrescriptionHd();
                newHd.setHospitalId(useObj.getHospital().getId());
                newHd.setNisNo(null);
                newHd.setPatientId(patient.getId());
                newHd.setDepartmentId(request.getDepartmentId());
                newHd.setDoctorName(useObj.getFirstName() + " " + useObj.getLastName());
                newHd.setPrescriptionDate(LocalDateTime.now());
                newHd.setStatus("n");
                newHd.setCreatedBy(useObj.getFirstName() + " " + useObj.getLastName());
                newHd.setIssuedBy(null);
                newHd.setIssuedDate(null);
                newHd.setTotalCost(BigDecimal.ZERO);
                newHd.setTotalGst(BigDecimal.ZERO);
                newHd.setTotalDiscount(BigDecimal.ZERO);
                newHd.setNetAmount(BigDecimal.ZERO);

                PatientPrescriptionHd savedHd = patientPrescriptionHdRepository.save(newHd);
                headerIdToUse = savedHd.getPrescriptionHdId();
            }

            Long finalHeaderId = headerIdToUse;
            treatmentObj.stream()
                    .filter(Objects::nonNull)
                    .filter(t -> t.getTreatmentId() == null)
                    .forEach(trt -> {
                        PatientPrescriptionDt dt = new PatientPrescriptionDt();
                        dt.setPrescriptionHdId(finalHeaderId);
                        dt.setItemId(trt.getDrugId());
                        dt.setDosage(trt.getDosage());

                        if (trt.getFrequency() == null) {
                            dt.setFrequency(null);
                        } else {
                            dt.setFrequency(masFrequencyRepository.findById(Long.valueOf(trt.getFrequency())).get().getFrequencyName());
                        }

                        dt.setDays(trt.getDays());

                        if (trt.getTotal() == null) {
                            dt.setTotal(BigDecimal.ZERO);
                        } else {
                            dt.setTotal(BigDecimal.valueOf(trt.getTotal()));
                        }

                        dt.setInstruction(trt.getInstruction());
                        dt.setStatus("n");

                        patientPrescriptionDtRepository.save(dt);
                    });
        }

        //  icd

        //  ICD DIAGNOSIS
        if (request.getIcdObj() != null) {

            for (RecallOpdPatientDetailRequest.IcdDiagnosis reqObj : request.getIcdObj()) {

                if (reqObj.getId() == null) {

                    DischargeIcdCode newIcd = new DischargeIcdCode();
                    newIcd.setIcdId(reqObj.getIcdId());
                    newIcd.setOpdPatientDetailsId(saved.getOpdPatientDetailsId());
                    newIcd.setVisitId(request.getVisitId());
                    newIcd.setAddEditById(useObj.getUserId());
                    newIcd.setAddEditDate(LocalDate.now());
                    newIcd.setAddEditTime(LocalTime.now().toString());

                    dischargeIcdCodeRepository.save(newIcd);
                }
                else {
                    DischargeIcdCode existing =
                            dischargeIcdCodeRepository.findById(reqObj.getId())
                                    .orElseThrow(() -> new RuntimeException("ICD not found: " + reqObj.getId()));

                    existing.setIcdId(reqObj.getIcdId());
                    existing.setAddEditById(useObj.getUserId());
                    existing.setAddEditDate(LocalDate.now());
                    existing.setAddEditTime(LocalTime.now().toString());

                    dischargeIcdCodeRepository.save(existing);
                }
            }
        }

        deleteDischargeIcd(request.getRemoveIcdIds());


        deleteDischargeIcd(request.getRemoveIcdIds());
        deleteOrderDetails(request.getRemovedInvestigationIds());
        deletePrescriptionDetails(request.getRemovedTreatmentIds());

        return ResponseUtils.createSuccessResponse(saved, new TypeReference<>() {});
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
                newDt.setFrequencyId(Long.valueOf(dt.getFrequency()));

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

