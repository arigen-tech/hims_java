package com.hims.dto;

import com.hims.entity.MasCountry;
import com.hims.entity.MasDistrict;
import com.hims.entity.MasRelation;
import com.hims.entity.MasState;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;

/**
 * DTO for {@link com.hims.entity.Patient}
 */
public record PatientDto( Long id, String uhidNo, String patientFn, String patientMn,
                         String patientLn, LocalDate patientDob, String patientAge, MasGenderDto patientGender,
                         String patientEmailId, String patientMobileNumber, String patientImage, String fileName,
                         MasRelationDto patientRelation, MasMaritalStatusDto patientMaritalStatus,
                         MasReligionDto patientReligion, String patientAddress1, String patientAddress2,
                         String patientCity, String patientPincode, MasDistrictDto patientDistrict,
                         MasStateDto patientState, MasCountryDto patientCountry, String pincode, String emerFn,
                         String emerLn, MasRelationDto emerRelation, String emerMobile, String nokFn, String nokLn,
                         String nokEmail, String nokMobileNumber, String nokAddress1, String nokAddress2,
                         String nokCity, MasDistrictDto nokDistrict, MasStateDto nokState, MasCountryDto nokCountry,
                         String nokPincode, MasRelationDto nokRelation, String patientStatus, LocalDate regDate,
                         MasHospitalDto patientHospital) implements Serializable {
}
