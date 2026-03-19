package com.hims.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
@Data
public class MasCrossMatchTypeRequest {

    @NotBlank(message = "CrossMatch code is required")
    @Size(max = 20, message = "CrossMatch code must not exceed 20 characters")
    private String crossMatchCode;

    @NotBlank(message = "CrossMatch name is required")
    @Size(max = 100, message = "CrossMatch name must not exceed 100 characters")
    private String crossMatchName;

    @Size(max = 300, message = "Description must not exceed 300 characters")
    private String description;

    @NotNull(message = "Turnaround time is required")
    @Min(value = 0, message = "Turnaround time cannot be negative")
    private Integer turnaroundTimeMin;

    @NotNull(message = "Charge amount is required")
    private BigDecimal chargeAmount;

    @NotBlank(message = "Emergency allowed is required")
    private String isEmergencyAllowed;


}
