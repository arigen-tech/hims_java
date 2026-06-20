package com.hims.m1.abdm_request;

import lombok.Data;

import java.util.List;



@Data
public class AbhaSendOtpRequest {


    private List<String> scope;
    private String loginHint;
    private String loginId;
    private String otpSystem;

}
