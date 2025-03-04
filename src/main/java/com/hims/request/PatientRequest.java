package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class PatientRequest {
    private Long id;
    private String uhidNo;
    private String patientFn;
    private String patientMn;
    private String patientLn;
    private LocalDate patientDob;
    private String patientAge;
    private Long patientGenderId;
    private String patientEmailId;
    private String patientMobileNumber;
    private String patientImage;
    private String fileName;
    private Long patientRelationId;
    private Long patientMaritalStatusId;
    private Long patientReligionId;
    private String patientAddress1;
    private String patientAddress2;
    private String patientCity;
    private String patientPincode;
    private Long patientDistrictId;
    private Long patientStateId;
    private Long patientCountryId;
    private String pincode;
    private String emerFn;
    private String emerLn;
    private Long emerRelationId;
    private String emerMobile;
    private String nokFn;
    private String nokLn;
    private String nokEmail;
    private String nokMobileNumber;
    private String nokAddress1;
    private String nokAddress2;
    private String nokCity;
    private Long nokDistrictId;
    private Long nokStateId;
    private Long nokCountryId;
    private String nokPincode;
    private Long nokRelationId;
    private String patientStatus;
    private LocalDate regDate;
    private String lastChgBy;
    private Long patientHospitalId;
}
