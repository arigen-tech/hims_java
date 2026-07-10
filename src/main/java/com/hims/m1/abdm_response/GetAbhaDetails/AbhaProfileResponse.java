package com.hims.m1.abdm_response.GetAbhaDetails;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AbhaProfileResponse {


    @JsonProperty("ABHANumber")
    private String abhaNumber;

    private String preferredAbhaAddress;
    @JsonProperty("mobileNo")
    private String mobile;
    private String address;
    private String pincode;
    @JsonProperty("aadhaarNo")
    private String addharNo;
    private String districtName;
    private String stateName;
    private String districtCode;
    private String stateCode;
    private String firstName;
    private String middleName;
    private String lastName;
    private String name;
    private String yearOfBirth;
    private String dayOfBirth;
    private String monthOfBirth;
    private String gender;
    private String profilePhoto;
    private String subdistrictName;

    private List<String> authMethods;

    private boolean kycVerified;
    private String verificationStatus;
    private String verificationType;

    private LocalizedDetails localizedDetails;

    private String createdDate;
    private Map<String, Object> tags;

}
