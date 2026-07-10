package com.hims.m1.abdm_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class VerifyOtpAbhaAddressProfileAccount {


    private String abhaNumber;
    private String abhaAddress;
    private String fullName;
    private String profilePhoto;
    private String status;
    private String kycStatus;

}

