package com.hims.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MedicinePrescriptionRequest {
    @NotNull(message = "Prescription ID is required")
    private Long prescriptionId;
    private String stopReason;
}
