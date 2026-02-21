package com.hims.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PatientResponseDTO {

    private Long id;
    private String uhidNo;
    private String fullName;
    private LocalDate patientDob;
    private String patientAge;
    private Long genderId;
    private String genderName;
    private String patientEmailId;
    private String patientMobileNumber;
    private String patientAddress1;
    private String patientAddress2;
    private String patientCity;
    private String patientPincode;
    private Long districtId;
    private String districtName;
    private Long stateId;
    private String stateName;
    private Long countryId;
    private String countryName;
    private String patientStatus;
    private LocalDate regDate;
    private String emerFn;
    private String emerLn;
    private String emerMobile;
    private String nokFn;
    private String nokLn;
    private String nokMobileNumber;

}
