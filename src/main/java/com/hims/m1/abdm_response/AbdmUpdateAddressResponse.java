package com.hims.m1.abdm_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AbdmUpdateAddressResponse {

    private String txnId;
    private String healthIdNumber;
    private String preferredAbhaAddress;

}