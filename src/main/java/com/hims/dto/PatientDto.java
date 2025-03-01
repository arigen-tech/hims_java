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
public record PatientDto(Long id, @Size(max = 50) String uhidNo, @Size(max = 50) String pFn, @Size(max = 50) String pMn,
                         @Size(max = 30) String pLn, LocalDate pDob, @Size(max = 50) String pAge, MasGenderDto pGender,
                         @Size(max = 70) String pEmailId, @Size(max = 20) String pMobileNumber,
                         @Size(max = 255) String patientImage, @Size(max = 50) String fileName,
                         MasRelationDto pRelation, MasMaritalStatusDto pMaritalStatus, MasReligionDto pReligion,
                         @Size(max = 500) String pAddress1, @Size(max = 500) String pAddress2,
                         @Size(max = 100) String pCity, @Size(max = 10) String pPincode, MasDistrictDto pDistrict,
                         MasStateDto pState, MasCountryDto pCountry, @Size(max = 8) String pincode,
                         @Size(max = 50) String emerFn, @Size(max = 50) String emerLn, MasRelationDto emerRelation,
                         @Size(max = 20) String emerMobile, @Size(max = 50) String nokFn, @Size(max = 50) String nokLn,
                         @Size(max = 70) String nokEmail, @Size(max = 20) String nokMobileNumber,
                         @Size(max = 500) String nokAddress1, @Size(max = 500) String nokAddress2,
                         @Size(max = 100) String nokCity, MasDistrict nokDistrict, MasState nokState,
                         MasCountry nokCountry, @Size(max = 8) String nokPincode, MasRelation nokRelation,
                         @Size(max = 20) String patientStatus, LocalDate regDate, @NotNull Instant createdOn,
                         @NotNull Instant updatedOn, @Size(max = 200) String lastChgBy,
                         MasHospitalDto pHospital) implements Serializable {
}
