package com.hims.m1.abdm_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AbhaProfileViaAbhaAddress {

    private String abhaAddress;
    private String abhaNumber;

    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;

    private String gender;

    private String dateOfBirth;
    private String dayOfBirth;
    private String monthOfBirth;
    private String yearOfBirth;

    private String mobile;

    private Boolean emailVerified;
    private Boolean mobileVerified;

    private String kycStatus;
    private String status;

    private String profilePhoto; // Base64

    private String address;

    private String stateName;
    private String stateCode;

    private String districtName;
    private String districtCode;

    private String subDistrictName;
    private String pinCode;

    private List<String> authMethods;

    private Integer abhaLinkedCount;
}
