package com.hims.mapper;


import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import com.hims.entity.User;
import com.hims.entity.Visit;
import com.hims.entity.repository.*;
import com.hims.exception.SDDException;
import com.hims.request.OpdPatientDetailCreateRequest;
import com.hims.response.OpdPatientDetailResponseDTO;
import com.hims.response.OpdPatientVitalResponse;
import com.hims.utils.AuthUtil;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class OpdPatientDetailMapper {

    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private MasDepartmentRepository departmentRepository;

    @Autowired
    private MasHospitalRepository hospitalRepository;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private AuthUtil authUtil;


    public OpdPatientDetailResponseDTO mapToDTO(OpdPatientDetail opd) {

        return OpdPatientDetailResponseDTO.builder()
                .opdPatientDetailsId(opd.getOpdPatientDetailsId())
                .height(opd.getHeight())
                .weight(opd.getWeight())
                .pulse(opd.getPulse())
                .temperature(opd.getTemperature())
                .bmi(opd.getBmi())
                .bpSystolic(opd.getBpSystolic())
                .bpDiastolic(opd.getBpDiastolic())
                .presentComplaints(opd.getPresentComplaints())
                .workingDiag(opd.getWorkingDiag())
                .icdDiag(opd.getIcdDiag())
                .opdDate(opd.getOpdDate())

                .patientId(opd.getPatient() != null ?
                        opd.getPatient().getId() : null)
                .patientName(opd.getPatient() != null ?
                        opd.getPatient().getFullName() : null)

                .visitId(opd.getVisit() != null ?
                        opd.getVisit().getId() : null)

                .departmentId(opd.getDepartment() != null ?
                        opd.getDepartment().getId() : null)
                .departmentName(opd.getDepartment() != null ?
                        opd.getDepartment().getDepartmentName() : null)

                .doctorId(opd.getDoctor() != null ?
                        opd.getDoctor().getUserId() : null)
                .doctorName(opd.getDoctor() != null ?
                        opd.getDoctor().getFirstName() : null)

                .followUpFlag(opd.getFollowUpFlag())
                .followUpDays(opd.getFollowUpDays())
                .followUpDate(opd.getFollowUpDate())

                .admissionFlag(opd.getAdmissionFlag())
                .build();
    }


    public static OpdPatientVitalResponse mapToVitalResponse(OpdPatientDetail opd) {
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


    public void mapBasicVitalDetails(OpdPatientDetail opd, OpdPatientDetailCreateRequest request) {
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
        opd.setLastChgBy(Objects.requireNonNull(authUtil.getCurrentUser()).getFullName());
    }

    public void mapClinicalDetails(OpdPatientDetail opd, OpdPatientDetailCreateRequest request) {
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

    public void mapGeneralDetails(OpdPatientDetail opd, Patient patient, Visit visit, User user, Long deptId) {
        opd.setPatient(patient);
        opd.setVisit(visit);
        opd.setOpdDate(Instant.now());
        opd.setHospital(user.getHospital());
        opd.setDoctor(user);
        opd.setDepartment(departmentRepository.findById(deptId).orElseThrow(() -> new SDDException("department", 404, "Department not found")));
        opd.setLastChgBy(user.getUsername());
        opd.setLastChgDate(Instant.now());
    }

    public <T, R> List<R> mapInvestigations(
            List<T> source,
            Function<T, R> mapper) {

        if (source == null) {
            return Collections.emptyList();
        }

        return source.stream()
                .map(mapper)
                .toList();
    }

}
