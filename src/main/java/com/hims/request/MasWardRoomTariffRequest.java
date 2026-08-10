package com.hims.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MasWardRoomTariffRequest {

    @NotNull(message = "Ward ID is required")
    @Positive(message = "Ward ID must be positive")
    private Long wardId;

    @NotNull(message = "Room ID is required")
    @Positive(message = "Room ID must be positive")
    private Long roomId;

    @NotNull(message = "Tariff is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Tariff must be greater than 0")
    private BigDecimal tariff;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;
}