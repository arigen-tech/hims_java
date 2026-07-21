package com.hims.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IpIntakeOutputEntryRequest {
    @NotBlank(message = "IO type is required")
    @Size(max = 1, message = "IO type must be I or O")
    private String ioType;

    // Required when ioType = I
    private Long intakeTypeId;

    // Required when ioType = I
    private Long intakeItemId;

    // Required when ioType = O
    private Long outputTypeId;

    // Mainly used for intake
    private Long routeId;

    private BigDecimal quantity;

    private String unit;

    private LocalDateTime observationDatetime;

   // private String shift;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;
}