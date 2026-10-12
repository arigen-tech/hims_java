package com.hims.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PatientFamilyMemberRegistrationRequest {
    @NotBlank
    @Size(max = 50)
    private String patientFn;
    @Size(max = 50)
    private String patientMn;
    @Size(max = 30)
    private String patientLn;
    private LocalDate patientDob;
    @Size(max = 50)
    private String patientAge;
    private Long patientGenderId;
    private Long bloodGroupId;
    @Size(max = 70)
    @Email
    private String patientEmailId;
    @NotBlank
    @Size(max = 20)
    private String patientMobileNumber;
    @NotNull
    private Long patientRelationId;
    private Long patientMaritalStatusId;
    @Size(max = 50)
    private String emerFn;
    @Size(max = 30)
    private String emerLn;
    @Size(max = 20)
    private String emerMobile;
    @Size(max = 100)
    private String patientAddress1;
    @Size(max = 100)
    private String patientAddress2;
    private Long patientCountryId;
    private Long patientStateId;
    private Long patientDistrictId;
    @Size(max = 10)
    private String patientPincode;
    @Size(max = 50)
    private String patientCity;
}
