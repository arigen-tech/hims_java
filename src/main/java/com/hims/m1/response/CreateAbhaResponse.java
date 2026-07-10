package com.hims.m1.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hims.m1.abdm_response.createAbha.CreateAbhaProfileResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateAbhaResponse {
    private Boolean isNew;
    private Boolean isMatchToAadhaar;
    private String txnId;
    private String xToken;
    private Integer expiresIn;
    private String refreshToken;
    private Integer refreshExpiresIn;
    String isType;

    @JsonProperty("ABHAProfile")
    private CreateAbhaProfileResponse ABHAProfile;

}
