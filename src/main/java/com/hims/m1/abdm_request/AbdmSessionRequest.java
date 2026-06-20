package com.hims.m1.abdm_request;

import lombok.Data;

@Data
public class AbdmSessionRequest {
    private String clientId;
    private String clientSecret;
    private String grantType;
}