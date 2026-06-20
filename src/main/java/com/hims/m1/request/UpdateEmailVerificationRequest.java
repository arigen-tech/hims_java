package com.hims.m1.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateEmailVerificationRequest {


    @NotNull(message = "Email Id by cannot be null or empty.")
    String emailId;

    @NotNull(message = "x-Token Id by cannot be null or empty.")
    String xToken;


}
