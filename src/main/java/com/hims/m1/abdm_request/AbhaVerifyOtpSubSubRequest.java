package com.hims.m1.abdm_request;

import lombok.Data;

import java.util.List;


@Data
public class AbhaVerifyOtpSubSubRequest {


    private String txnId;
    private String otpValue;


}
