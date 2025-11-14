package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.*;
import com.hims.entity.repository.*;
import com.hims.request.OpdPatientDetailFinalRequest;
import com.hims.response.ApiResponse;
import com.hims.response.OpdPatientRecallResponce;
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
import java.util.*;
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

    @Override
    public ApiResponse<List<OpdPatientRecallResponce>> getRecallVisit(String name, String mobile, LocalDate visitDate) {

        if (visitDate == null && mobile == null && name == null) {
            visitDate = LocalDate.now();
        }

        if (mobile == null || mobile.trim().isEmpty()) {
            mobile = "";
        } else {
            mobile = mobile.trim();
        }

        if (name == null || name.trim().isEmpty()) {
            name = "";
        } else {
            name = name.trim();
        }

        List<Visit> recallVisit = visitRepository.searchRecallVisits(visitDate, mobile, name);

        List<OpdPatientRecallResponce> responseList = new ArrayList<>();

        for (Visit visitObj : recallVisit) {

            Patient patientObj = visitObj.getPatient();
            if (patientObj == null) continue;

            User docObj = visitObj.getDoctor();
            MasDepartment deptObj = visitObj.getDepartment();
            MasGender genderObj = patientObj.getPatientGender();
            MasRelation relationObj = patientObj.getPatientRelation();

            OpdPatientDetail opdPatientObj =
                    opdPatientDetailRepository.findByVisitId(visitObj.getId());

            DgOrderHd dgOrderHd =
                    dgOrderHdRepo.findByVisitId(visitObj);

            List<DgOrderDt> dgOrderDtList = dgOrderHd != null
                    ? dgOrderDtRepo.findByOrderhdId(dgOrderHd)
                    : Collections.emptyList();

            PatientPrescriptionHd patientPrescHdObj =
                    patientPrescriptionHdRepository.findByPatientId(patientObj.getId());

            List<PatientPrescriptionDt> prescDtList =
                    patientPrescHdObj != null
                            ? patientPrescriptionDtRepository.findByPrescriptionHdId(
                            patientPrescHdObj.getPrescriptionHdId())
                            : Collections.emptyList();

            // ------------ Build Response ----------
            OpdPatientRecallResponce response = new OpdPatientRecallResponce();

            // Build patient name
            String patientName = buildFullName(
                    patientObj.getPatientFn(),
                    patientObj.getPatientMn(),
                    patientObj.getPatientLn()
            );
            response.setPatientName(patientName);

            response.setMobileNo(patientObj.getPatientMobileNumber());
            response.setGender(genderObj != null ? genderObj.getGenderName() : null);
            response.setRelation(relationObj != null ? relationObj.getRelationName() : null);
            response.setDob(patientObj.getPatientDob());
            response.setAge(patientObj.getPatientAge());
            response.setDeptId(deptObj != null ? deptObj.getId() : null);
            response.setDeptName(deptObj != null ? deptObj.getDepartmentName() : null);
            response.setDocterId(docObj != null ? docObj.getUserId() : null);

            // Build doctor name
            if (docObj != null) {
                String doctorName = buildFullName(
                        docObj.getFirstName(),
                        docObj.getMiddleName(),
                        docObj.getLastName()
                );
                response.setDocterName(doctorName);
            }

            response.setVisitId(visitObj.getId());
            response.setPatientId(patientObj.getId());

            // --------------------- OPD DATA --------------------
            if (opdPatientObj != null) {
                response.setOpdPatientId(opdPatientObj.getOpdPatientDetailsId());
                response.setOpdDate(opdPatientObj.getOpdDate());
                response.setPastMedicalHistory(opdPatientObj.getPastMedicalHistory());
                response.setFamilyHistory(opdPatientObj.getFamilyHistory());
                response.setPatientSignsSymptoms(opdPatientObj.getPatientSignsSymptoms());
                response.setClinicalExamination(opdPatientObj.getClinicalExamination());
                response.setHeight(opdPatientObj.getHeight());
                response.setIdealWeight(opdPatientObj.getIdealWeight());
                response.setWeight(opdPatientObj.getWeight());
                response.setPulse(opdPatientObj.getPulse());
                response.setTemperature(opdPatientObj.getTemperature());
                response.setRr(opdPatientObj.getRr());
                response.setBmi(opdPatientObj.getBmi());
                response.setSpo2(opdPatientObj.getSpo2());
                response.setBpSystolic(opdPatientObj.getBpSystolic());
                response.setBpDiastolic(opdPatientObj.getBpDiastolic());
                response.setMlcFlag(opdPatientObj.getMlcFlag());
                response.setWorkingDiag(opdPatientObj.getWorkingDiag());

                if (opdPatientObj.getIcdDiag() != null && !opdPatientObj.getIcdDiag().isEmpty()) {
                    List<String> icdList = Arrays.stream(opdPatientObj.getIcdDiag().split(","))
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .toList();
                    response.setIcdDiag(icdList);
                }

                response.setLabFlag(opdPatientObj.getLabFlag());
                response.setRadioFlag(opdPatientObj.getRadioFlag());
            }

            // --------------------- DG ORDER HD MAP ---------------------
            if (dgOrderHd != null) {
                OpdPatientRecallResponce.NewDgOrderHd hd = new OpdPatientRecallResponce.NewDgOrderHd();
                hd.setDgOrderHdId(dgOrderHd.getId());
                hd.setOrderDate(dgOrderHd.getOrderDate());
                hd.setOrderNo(dgOrderHd.getOrderNo());
                hd.setOrderStatus(dgOrderHd.getOrderStatus());
                hd.setCollectionStatus(dgOrderHd.getCollectionStatus());
                hd.setPaymentStatus(dgOrderHd.getPaymentStatus());
                hd.setAppointmentDate(dgOrderHd.getAppointmentDate());
                response.setDgOrderHd(hd);
            }

            // --------------------- DG ORDER DT LIST MAP ---------------------
            List<OpdPatientRecallResponce.NewDgOrderDt> dtList = new ArrayList<>();

            for (DgOrderDt dt : dgOrderDtList) {
                OpdPatientRecallResponce.NewDgOrderDt newDt = new OpdPatientRecallResponce.NewDgOrderDt();

                newDt.setDgOrderDtId(dt.getId());
                newDt.setOrderQty(dt.getOrderQty());
                newDt.setOrderStatus(dt.getOrderStatus());
                newDt.setAppointmentDate(dt.getAppointmentDate());
                newDt.setInvestigationId(dt.getInvestigationId());
                newDt.setBillingStatus(dt.getBillingStatus());
                newDt.setPackageId(dt.getPackageId());
                newDt.setBillingHd(dt.getBillingHd());
                newDt.setInvestigationName(
                        dt.getInvestigationId() != null
                                ? dt.getInvestigationId().getInvestigationName()
                                : null
                );

                dtList.add(newDt);
            }

            response.setDgOrderDts(dtList);

            // -------------------- PRESCRIPTION HD MAP --------------------
            if (patientPrescHdObj != null) {
                OpdPatientRecallResponce.NewDPatientPrescriptionHd hd =
                        new OpdPatientRecallResponce.NewDPatientPrescriptionHd();

                hd.setPrescriptionHdId(patientPrescHdObj.getPrescriptionHdId());
                hd.setStatus(patientPrescHdObj.getStatus());
                hd.setPrescriptionDate(patientPrescHdObj.getPrescriptionDate());

                response.setPatientPrescriptionHd(hd);
            }

            // -------------------- PRESCRIPTION DT MAP --------------------
            List<OpdPatientRecallResponce.NewDPatientPrescriptionDt> newPrescList = new ArrayList<>();

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
                newDt.setItemName(masStoreItemRepository.findById(dt.getItemId()).get().getNomenclature());
                newPrescList.add(newDt);
            }

            response.setPatientPrescriptionDts(newPrescList);

            responseList.add(response);
        }

        return ResponseUtils.createSuccessResponse(responseList, new TypeReference<>() {});
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
}

