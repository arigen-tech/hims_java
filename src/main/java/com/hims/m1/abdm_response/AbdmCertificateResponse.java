package com.hims.m1.abdm_response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class AbdmCertificateResponse {

    private String publicKey;
    private String encryptionAlgorithm;
    private AbdmSessionApiResponse session;

}