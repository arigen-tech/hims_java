package com.hims.m1.abdm_response.UpdateEmail;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class EmailLinkResponse {

    private String txnId;
    private String authResult;
    private String message;

    private List<Account> accounts;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Account {

        @JsonProperty("ABHANumber")
        private String abhaNumber;
    }
}