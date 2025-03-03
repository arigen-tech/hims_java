package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import com.hims.entity.repository.OpdPatientDetailRepository;
import com.hims.entity.repository.PatientRepository;
import com.hims.response.ApiResponse;
import com.hims.service.PatientService;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private OpdPatientDetailRepository opdPatientDetailRepository;

    @Override
    public ApiResponse<Patient> registerPatientWithOpd(Patient patient, OpdPatientDetail opdPatientDetail) {
        Optional<Patient> existingPatient = patientRepository.findByUniqueCombination(
                patient.getPatientFn(),
                patient.getPatientLn(),
                patient.getPatientGender(),
                patient.getPatientDob() != null ? patient.getPatientDob() : null,
                patient.getPatientAge(),
                patient.getPatientMobileNumber(),
                patient.getPatientRelation()
        );

        if (existingPatient.isPresent()) {
            return ResponseUtils.createFailureResponse(patient, new TypeReference<>() {},
                    "Patient already Registered",500);
        }
        patient.setUhidNo(generateUhid(patient));
        patient = patientRepository.save(patient); // Save patient

        // Convert OPD DTO to Entity
        opdPatientDetail.setPatient(patient); // Associate patient
        opdPatientDetailRepository.save(opdPatientDetail); // Save OPD details

        // Convert saved entity back to DTO and return
        return ResponseUtils.createSuccessResponse(patient, new TypeReference<>() {
        });
    }

    private String generateUhid(Patient patient) {
        List<Patient> existing =patientRepository.findByPatientMobileNumberAndPatientRelation(patient.getPatientMobileNumber(), patient.getPatientRelation());
        return (patient.getPatientMobileNumber()+patient.getPatientRelation().getCode()+(existing.size()+1));
    }
}
