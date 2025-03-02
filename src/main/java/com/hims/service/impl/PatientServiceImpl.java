package com.hims.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.dto.OpdPatientDetailDto;
import com.hims.dto.PatientDto;
import com.hims.entity.OpdPatientDetail;
import com.hims.entity.Patient;
import com.hims.entity.repository.OpdPatientDetailRepository;
import com.hims.entity.repository.PatientRepository;
import com.hims.mapper.MasGenderMapper;
import com.hims.mapper.MasRelationMapper;
import com.hims.mapper.OpdPatientDetailMapper;
import com.hims.response.ApiResponse;
import com.hims.service.PatientService;
import com.hims.mapper.PatientMapper;
import com.hims.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private OpdPatientDetailRepository opdPatientDetailRepository;
    @Autowired
    private  PatientMapper patientMapper;
    @Autowired
    private MasGenderMapper genderMapper;
    @Autowired
    private MasRelationMapper relationMapper;
    @Autowired
    private OpdPatientDetailMapper opdPatientDetailMapper;
    @Override
    public ApiResponse<PatientDto> registerPatientWithOpd(PatientDto patientDto, OpdPatientDetailDto opdPatientDetailDto) {
        Patient patient = patientMapper.toEntity(patientDto);
        Optional<Patient> existingPatient = patientRepository.findByUniqueCombination(
                patientDto.patientFn(),
                patientDto.patientLn(),
                genderMapper.toEntity(patientDto.patientGender()),
                patientDto.patientDob() != null ? patientDto.patientDob().toString() : null,
                patientDto.patientAge(),
                patientDto.patientMobileNumber(),
                relationMapper.toEntity(patientDto.patientRelation())
        );

        if (existingPatient.isPresent()) {
            return ResponseUtils.createFailureResponse(patientDto, new TypeReference<>() {},
                    "Patient already Registered",500);
        }

        patient = patientRepository.save(patient); // Save patient

        // Convert OPD DTO to Entity
        OpdPatientDetail opdPatientDetail = opdPatientDetailMapper.toEntity(opdPatientDetailDto);
        opdPatientDetail.setPatient(patient); // Associate patient
        opdPatientDetailRepository.save(opdPatientDetail); // Save OPD details

        // Convert saved entity back to DTO and return
        return ResponseUtils.createSuccessResponse(patientMapper.toDto(patient), new TypeReference<>() {
        });
    }
}
