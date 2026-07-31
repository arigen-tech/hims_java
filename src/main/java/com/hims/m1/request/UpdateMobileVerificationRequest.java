package com.hims.m1.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateMobileVerificationRequest {

    @NotNull(message = "Email Id by cannot be null or empty.")
    String mobileNumber;

    @NotNull(message = "x-Token Id by cannot be null or empty.")
    String xToken;


}
