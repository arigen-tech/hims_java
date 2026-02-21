package com.hims.mapper;

import com.hims.entity.Patient;
import com.hims.response.PatientResponseDTO;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public static PatientResponseDTO mapToDTO(Patient patient) {

        return PatientResponseDTO.builder()
                .id(patient.getId())
                .uhidNo(patient.getUhidNo())
                .fullName(patient.getFullName())
                .patientDob(patient.getPatientDob())
                .patientAge(patient.getPatientAge())

                .genderId(patient.getPatientGender() != null ?
                        patient.getPatientGender().getId() : null)
                .genderName(patient.getPatientGender() != null ?
                        patient.getPatientGender().getGenderName() : null)

                .patientEmailId(patient.getPatientEmailId())
                .patientMobileNumber(patient.getPatientMobileNumber())

                .patientAddress1(patient.getPatientAddress1())
                .patientAddress2(patient.getPatientAddress2())
                .patientCity(patient.getPatientCity())
                .patientPincode(patient.getPatientPincode())

                .districtId(patient.getPatientDistrict() != null ?
                        patient.getPatientDistrict().getId() : null)
                .districtName(patient.getPatientDistrict() != null ?
                        patient.getPatientDistrict().getDistrictName() : null)

                .stateId(patient.getPatientState() != null ?
                        patient.getPatientState().getId() : null)
                .stateName(patient.getPatientState() != null ?
                        patient.getPatientState().getStateName() : null)

                .countryId(patient.getPatientCountry() != null ?
                        patient.getPatientCountry().getId() : null)
                .countryName(patient.getPatientCountry() != null ?
                        patient.getPatientCountry().getCountryName() : null)

                .patientStatus(patient.getPatientStatus())
                .regDate(patient.getRegDate())

                .emerFn(patient.getEmerFn())
                .emerLn(patient.getEmerLn())
                .emerMobile(patient.getEmerMobile())

                .nokFn(patient.getNokFn())
                .nokLn(patient.getNokLn())
                .nokMobileNumber(patient.getNokMobileNumber())

                .build();
    }
}
