package com.hims.m1.abdm_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AbdmAddressVerifyResponse {


    private String message;
    private String authResult;
    private List<VerifyOtpAbhaAddressProfileAccount> users;
    private TokenInfo tokens;

}