package com.hims.m1.abdm_response.UpdateMobile;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class UpdateMobileResponse {


    private String txnId;
    private String authResult;
    private String message;
    private List<UpdateMobileSubResponse> accounts;
}
