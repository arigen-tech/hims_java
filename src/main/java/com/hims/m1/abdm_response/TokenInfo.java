package com.hims.m1.abdm_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class TokenInfo {

    private String token;
    private Integer expiresIn;
    private String refreshToken;
    private Integer refreshExpiresIn;
}
