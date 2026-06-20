package com.hims.m1.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class VerifingAbhaSendOTPRequest {


    @NotBlank(message = "OTP cannot be null or empty.")
    String otp;

    @NotBlank(message = "TnxID cannot be null or empty.")
    String tnxId;

    @NotBlank(message = "Mobile Number cannot be null or empty.")
    String mobileNumber;



}
