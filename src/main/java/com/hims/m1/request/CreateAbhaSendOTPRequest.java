package com.hims.m1.request;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateAbhaSendOTPRequest {

    @NotNull(message = "Aadhaar Number by cannot be null or empty.")
    String aadhaarNumber;

    @NotNull(message = "Consent Name by cannot be null or empty.")
    String consentName;

    @NotNull(message = "Consent1 by cannot be null or empty.")
    String consent1;

    @NotNull(message = "Consent2 by cannot be null or empty.")
    String consent2;

    @NotNull(message = "Consent3 by cannot be null or empty.")
    String consent3;

    @NotNull(message = "Consent4 by cannot be null or empty.")
    String consent4;

    @NotNull(message = "Consent5 by cannot be null or empty.")
    String consent5;

    @NotNull(message = "Consent6 by cannot be null or empty.")
    String consent6;

    @NotNull(message = "Consent7 by cannot be null or empty.")
    String consent7;

    @NotNull(message = "Key cannot be null or empty.")
    String key;


}
