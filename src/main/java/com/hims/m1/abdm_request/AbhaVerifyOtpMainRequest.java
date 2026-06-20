package com.hims.m1.abdm_request;

import lombok.Data;

import java.util.List;


@Data
public class AbhaVerifyOtpMainRequest {


    private List<String> scope;
    private AbhaVerifyOtpSubRequest authData;
}
