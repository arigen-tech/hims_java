package com.hims.m1.abdm_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AbdmVerifyResponse {

    @JsonProperty("refreshToken")
    private String refreshToken;
    private String txnId;
    private String authResult;
    private String message;
    private String token;
    private int expiresIn;
    private List<VerifyOtpProfileAccount> accounts;
}