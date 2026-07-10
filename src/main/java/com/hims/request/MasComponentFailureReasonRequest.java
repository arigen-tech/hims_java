package com.hims.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MasComponentFailureReasonRequest {

    @NotBlank(message = "Failure reason code is required")
    @Size(max = 50, message = "Failure reason code must not exceed 50 characters")
    private String failureReasonCode;

    @NotBlank(message = "Failure reason name is required")
    @Size(max = 100, message = "Failure reason name must not exceed 100 characters")
    private String failureReasonName;

    @Size(max = 300, message = "Description must not exceed 300 characters")
    private String description;


}