package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.OpdPatientDetailFinalRequest;
import com.hims.response.ApiResponse;
import com.hims.service.OpdPatientDetailService;
import com.hims.utils.AuthUtil;
import com.hims.utils.RandomNumGenerator;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpdPatientDetailServiceImpl implements OpdPatientDetailService {



    private final OpdPatientDetailRepository opdPatientDetailRepository;
    private final PatientRepository patientRepository;
    private final VisitRepository visitRepository;
    private final MasDepartmentRepository departmentRepository;
    private final MasHospitalRepository hospitalRepository;
    private final UserRepo userRepository;

    private final DgMasInvestigationRepository dgMasInvestigationRepository;
    private final LabHdRepository dgOrderHdRepo;

    private final LabDtRepository dgOrderDtRepo;

    private final PatientPrescriptionHdRepository patientPrescriptionHdRepository;

    private final PatientPrescriptionDtRepository patientPrescriptionDtRepository;

    private final RandomNumGenerator randomNumGenerator;

    private final AuthUtil authUtil;

    private final MasStoreItemRepository masStoreItemRepository;

    public String createOrderNum() {
        return randomNumGenerator.generateOrderNumber("OPD",true,true);
    }

//    @Override
//    @Transactional
//    public ApiResponse<OpdPatientDetail> createOpdPatientDetail(OpdPatientDetailFinalRequest request) {
//        log.info("start print ");
//        log.info(String.valueOf(request));
//        Long deptId = authUtil.getCurrentDepartmentId();
//        User useObj = authUtil.getCurrentUser();
//        OpdPatientDetail opdPatientDetail = new OpdPatientDetail();
//
//        //  Map vital details
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
//
//        // Diagnosis
//
//        if ((request.getWorkingDiag() != null && !request.getWorkingDiag().isBlank()) ||
//                (request.getIcdDiag() != null && !request.getIcdDiag().isEmpty())) {
//
//            opdPatientDetail.setWorkingDiag(request.getWorkingDiag());
//
//            if (request.getIcdDiag() != null && !request.getIcdDiag().isEmpty()) {
//                String icd = String.join(",", request.getIcdDiag());
//                opdPatientDetail.setIcdDiag(icd);
//            } else {
//                opdPatientDetail.setIcdDiag(null);
//            }
//
//        } else {
//            return ResponseUtils.createFailureResponse(
//                    null,
//                    new TypeReference<>() {},
//                    "One is mandatory: Working Diagnosis or ICD Diagnosis.",
//                    400
//            );
//        }
//
//
//
//
//        // Clinical history
//        opdPatientDetail.setPastMedicalHistory(request.getPastMedicalHistory());
//        opdPatientDetail.setFamilyHistory(request.getFamilyHistory());
////        opdPatientDetail.setPresentComplaints(request.getPresentComplaints());
//        opdPatientDetail.setClinicalExamination(request.getClinicalExamination());
//        opdPatientDetail.setPatientSignsSymptoms(request.getPatientSignsSymptoms());
//
//
//
//        //  Investigation
//
//        String OderNumOPD = createOrderNum();
//        Map<LocalDate, List<OpdPatientDetailFinalRequest.Investigation>> groupedByDate =
//                request.getInvestigation().stream()
//                        .collect(Collectors.groupingBy(OpdPatientDetailFinalRequest.Investigation::getInvestigationDate));
//
//
//        for (Map.Entry<LocalDate, List<OpdPatientDetailFinalRequest.Investigation>> entry : groupedByDate.entrySet()) {
//            LocalDate investigationDate = entry.getKey();
//            List<OpdPatientDetailFinalRequest.Investigation> invList = entry.getValue();
//
//            // Create and save DgOrderHd (header)
//            DgOrderHd dgOrderHd = new DgOrderHd();
//            dgOrderHd.setOrderDate(LocalDate.now());
//            dgOrderHd.setAppointmentDate(investigationDate);
//            dgOrderHd.setOrderNo(OderNumOPD);
//            dgOrderHd.setOrderStatus("n");
//            dgOrderHd.setCollectionStatus("n");
//            dgOrderHd.setPaymentStatus("n");
//            dgOrderHd.setDiscountId(1);
//            Optional<Patient> pObj = patientRepository.findById(request.getPatientId());
//            dgOrderHd.setPatientId(pObj.get());
//            dgOrderHd.setDepartmentId(Math.toIntExact(request.getDepartmentId()));
//            dgOrderHd.setHospitalId(Math.toIntExact(request.getHospitalId()));
//            Optional<Visit> VistObj = visitRepository.findById(request.getVisitId());
//            dgOrderHd.setVisitId(VistObj.get());
//            dgOrderHd.setLastChgBy(useObj.getFirstName()+" "+useObj.getLastName());
//            dgOrderHd.setCreatedBy(useObj.getFirstName()+" "+useObj.getLastName());
//            dgOrderHd.setCreatedOn(LocalDate.now());
//            dgOrderHd.setLastChgDate(LocalDate.now());
//            dgOrderHd.setLastChgTime(LocalTime.now().toString());
//
//
//            dgOrderHd = dgOrderHdRepo.save(dgOrderHd);
//
//            // For each investigation under this date
//            for (OpdPatientDetailFinalRequest.Investigation invObj : invList) {
//                DgOrderDt dgOrderDt = new DgOrderDt();
//                Optional<DgMasInvestigation> invObj1 = dgMasInvestigationRepository.findById(invObj.getId());
//                dgOrderDt.setInvestigationId(invObj1.get());
//                dgOrderDt.setOrderhdId(dgOrderHd);
//                dgOrderDt.setAppointmentDate(invObj.getInvestigationDate());
//                dgOrderDt.setLastChgBy(useObj.getFirstName()+" "+useObj.getLastName());
//                dgOrderDt.setCreatedBy(useObj.getFirstName()+" "+useObj.getLastName());
//                dgOrderDt.setLastChgDate(LocalDate.now());
//                dgOrderDt.setBillingStatus("n");
//                dgOrderDt.setOrderStatus("n");
//                dgOrderDt.setOrderQty(1);
//                dgOrderDt.setCreatedon(Instant.now());
//                dgOrderDt.setLastChgTime(LocalTime.now().toString());
//
//                dgOrderDt.setMainChargecodeId(invObj1.get().getMainChargeCodeId().getChargecodeId());
//                dgOrderDt.setSubChargeid(invObj1.get().getSubChargeCodeId().getSubId());
////                dt.setPackageId(pkgObj);
//
//                dgOrderDtRepo.save(dgOrderDt);
//
//            }
//        }
//
//
//        //        Treatment
//
//        PatientPrescriptionHd precHdObj = new PatientPrescriptionHd();
//        precHdObj.setHospitalId(useObj.getHospital().getId());
//        precHdObj.setNisNo(null);
//        precHdObj.setPatientId(useObj.getUserId());
//        precHdObj.setDepartmentId(authUtil.getCurrentDepartmentId());
//        precHdObj.setDoctorName(useObj.getFirstName()+" "+useObj.getLastName());
//        precHdObj.setPrescriptionDate(LocalDateTime.now());
//        precHdObj.setStatus("n");
//        precHdObj.setCreatedBy(useObj.getFirstName()+" "+useObj.getLastName());
//        precHdObj.setIssuedBy(null);
//        precHdObj.setIssuedDate(null);
//        precHdObj.setTotalCost(null);
//        precHdObj.setTotalGst(null);
//        precHdObj.setTotalDiscount(null);
//        precHdObj.setNetAmount(null);
//        PatientPrescriptionHd newPatieHd = patientPrescriptionHdRepository.save(precHdObj);
//
//
//        for (OpdPatientDetailFinalRequest.Treatment trtObj: request.getTreatment()) {
//
//            PatientPrescriptionDt precDtObj = new PatientPrescriptionDt();
//            precDtObj.setPrescriptionHdId(newPatieHd.getPrescriptionHdId());
//            precDtObj.setItemId(trtObj.getItemId());
//            precDtObj.setDosage(trtObj.getDosage());
//            precDtObj.setFrequency(trtObj.getFrequency());
//            precDtObj.setDays(trtObj.getDays());
//            precDtObj.setTotal(trtObj.getTotal());
//            precDtObj.setIssuedQty(null);
//            precDtObj.setRoute(null);
//            precDtObj.setInstruction(trtObj.getInstraction());
//            precDtObj.setUnitPrice(null);
//            precDtObj.setDiscount(null);
//            precDtObj.setGstRate(null);
//            precDtObj.setLineCost(null);
//            precDtObj.setSubstituteItemId(null);
//            precDtObj.setStatus("n");
//            precDtObj.setBatchNo(null);
//            precDtObj.setExpiryDate(null);
//            patientPrescriptionDtRepository.save(precDtObj);
//
//        }
//
//
//
//        // General details
//        opdPatientDetail.setOpdDate(request.getOpdDate());
//        opdPatientDetail.setVaration(request.getVaration());
//        opdPatientDetail.setFollowUpFlag(request.getFollowUpFlag());
//        opdPatientDetail.setFollowUpDays(request.getFollowUpDays());
//        opdPatientDetail.setTreatmentAdvice(request.getTreatmentAdvice());
//        opdPatientDetail.setSosFlag(request.getSosFlag());
//        opdPatientDetail.setRecmmdMedAdvice(request.getRecmmdMedAdvice());
//        opdPatientDetail.setMedicineFlag(request.getMedicineFlag());
//        opdPatientDetail.setLabFlag(request.getLabFlag());
//        opdPatientDetail.setRadioFlag(request.getRadioFlag());
//        opdPatientDetail.setReferralFlag(request.getReferralFlag());
//        opdPatientDetail.setPoliceStation(request.getPoliceStation());
//        opdPatientDetail.setPoliceName(request.getPoliceName());
//
//        if (request.getPatientId() != null) {
//            opdPatientDetail.setPatient(patientRepository.findById(request.getPatientId()).orElse(null));
//        }
//        if (request.getVisitId() != null) {
//            opdPatientDetail.setVisit(visitRepository.findById(request.getVisitId()).orElse(null));
//        }
//        if (deptId != null) {
//            opdPatientDetail.setDepartment(departmentRepository.findById(deptId).orElse(null));
//        }
//        if (useObj.getHospital().getId() != null) {
//            opdPatientDetail.setHospital(hospitalRepository.findById(useObj.getHospital().getId()).orElse(null));
//        }
//        if (useObj.getUserId() != null) {
//            opdPatientDetail.setDoctor(userRepository.findById(useObj.getUserId()).orElse(null));
//        }
//
//        opdPatientDetail.setLastChgDate(Instant.now());
//        opdPatientDetail.setLastChgBy(useObj.getUsername()); // or get from logged-in user
//
//        OpdPatientDetail saved = opdPatientDetailRepository.save(opdPatientDetail);
//
//        return ResponseUtils.createSuccessResponse(saved, new TypeReference<>() {
//        });
//    }


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

            opdPatientDetail.setWorkingDiag(request.getWorkingDiag());

            if (request.getIcdDiag() != null && !request.getIcdDiag().isEmpty()) {
                opdPatientDetail.setIcdDiag(String.join(",", request.getIcdDiag()));
            } else {
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

                // Validate Patient, Visit
                Patient patient = patientRepository.findById(request.getPatientId())
                        .orElseThrow(() -> new RuntimeException("Patient not found with ID: " + request.getPatientId()));

                Visit visit = visitRepository.findById(request.getVisitId())
                        .orElseThrow(() -> new RuntimeException("Visit not found with ID: " + request.getVisitId()));

                // Create DgOrderHd (Header)
                DgOrderHd dgOrderHd = new DgOrderHd();
                dgOrderHd.setOrderDate(LocalDate.now());
                dgOrderHd.setAppointmentDate(investigationDate);
                dgOrderHd.setOrderNo(orderNumOPD);
                dgOrderHd.setOrderStatus("n");
                dgOrderHd.setCollectionStatus("n");
                dgOrderHd.setPaymentStatus("n");
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

                // Save DgOrderDt (Details)
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
                    dgOrderDt.setBillingStatus("n");
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
            precHdObj.setCreatedBy(useObj.getFirstName() + " " + useObj.getLastName());
            precHdObj.setIssuedBy(null);
            precHdObj.setIssuedDate(null);
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

        OpdPatientDetail saved = opdPatientDetailRepository.save(opdPatientDetail);

        // ======================== UPDATE VISIT STATUS =====================
        Visit visit = visitRepository.findById(request.getVisitId())
                .orElseThrow(() -> new RuntimeException("Visit not found with ID: " + request.getVisitId()));
        visit.setVisitStatus("c");
        visitRepository.save(visit);

        return ResponseUtils.createSuccessResponse(saved, new TypeReference<>() {});
    }

    @Override
    public ApiResponse<List<Visit>> getActiveVisits() {
        List<Visit> activeVisits = visitRepository.findByVisitStatusAndBillingStatus("n", "y");
        return ResponseUtils.createSuccessResponse(activeVisits, new TypeReference<>() {});
    }

}

