package com.hims.m1.abdm_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class VerifyOtpProfileAccount {


    @JsonProperty("ABHANumber")
    private String abhaNumber;

    private String preferredAbhaAddress;
    private String name;
    private String gender;
    private String dob;
    private String verifiedStatus;
    private String verificationType;
    private String status;
    private String profilePhoto;
    private boolean kycVerified;
}

