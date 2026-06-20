package com.hims.m1.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hims.m1.abdm_response.MobilAbhaResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OtpSendResponse {

    private String txnId;
    private String otpType;
    String isType;
    private List<MobilAbhaResponse> abhaResponse;




    //Find Auth mod data

    private String healthIdNumber;
    private String abhaAddress;
    private List<String> authMethods;
    private List<String> blockedAuthMethods;
    private String status;
    private String message;
    private String fullName;
    private String mobile;


}