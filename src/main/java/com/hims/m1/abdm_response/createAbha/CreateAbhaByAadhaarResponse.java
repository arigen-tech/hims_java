package com.hims.m1.abdm_response.createAbha;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hims.m1.abdm_response.TokenInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CreateAbhaByAadhaarResponse {

    private String message;
    private String txnId;
    private TokenInfo tokens;

    @JsonProperty("ABHAProfile")
    private CreateAbhaProfileResponse ABHAProfile;
    private Boolean isNew;
    private Boolean isMatchToAadhaar;
}
