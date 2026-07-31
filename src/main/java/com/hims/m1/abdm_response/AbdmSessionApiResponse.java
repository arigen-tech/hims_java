package com.hims.m1.abdm_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AbdmSessionApiResponse {

    private String accessToken;

    private Integer expiresIn;

    private String tokenType;

    private String refreshToken;

    private String refreshExpiresIn;
}