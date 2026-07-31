package com.hims.m1.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AbhaProfileResponse {

    private String healthIdNumber;
    private String abhaAddress;
    private List<String> authMethods;
    private List<String> blockedAuthMethods;
    private String status;
    private String message;
    private String fullName;
    private String mobile;
}