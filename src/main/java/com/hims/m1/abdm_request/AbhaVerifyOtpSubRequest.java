package com.hims.m1.abdm_request;

import lombok.Data;

import java.util.List;


@Data
public class AbhaVerifyOtpSubRequest {


    private List<String> authMethods;
    private AbhaVerifyOtpSubSubRequest otp;
}
