package com.hims.m1.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateVerifyRequest {


    @NotBlank(message = "OTP cannot be null or empty.")
    String otp;

    @NotBlank(message = "TxnId ID by cannot be null or empty.")
    String txnId;

    @NotBlank(message = "x-Token ID by cannot be null or empty.")
    String xToken;


}
