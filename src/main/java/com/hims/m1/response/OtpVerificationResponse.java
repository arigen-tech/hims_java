package com.hims.m1.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hims.m1.abdm_response.VerifyOtpProfileAccount;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class OtpVerificationResponse {

    String isType;
    private String txnId;
    private String authResult;
    private String message;
    private String xToken;
    private int expiresIn;
    private List<VerifyOtpProfileAccount> accounts;
}