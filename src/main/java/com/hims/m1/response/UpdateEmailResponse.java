package com.hims.m1.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.hims.m1.abdm_response.UpdateEmail.EmailLinkResponse;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UpdateEmailResponse {


    private String txnId;
    private String authResult;
    private String message;
    String isType;
    private List<EmailLinkResponse.Account> accounts;

    @JsonProperty("ABHANumber")
    private String abhaNumber;
}
