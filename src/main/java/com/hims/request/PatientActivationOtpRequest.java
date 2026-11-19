package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PatientActivationOtpRequest {
    @NotNull
    private Long patientId;
    @NotBlank
    private String mobileNo;
    private String sessionId;
    private String otp;
}
