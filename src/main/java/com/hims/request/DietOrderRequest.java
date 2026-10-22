package com.hims.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DietOrderRequest {
    @NotNull(message = "Inpatient ID is required")
    @Positive(message = "Inpatient ID must be greater than 0")
    private Long InpatientId;

    @NotNull(message = "Diet type ID is required")
    @Positive(message = "Diet type ID must be greater than 0")
    private Long dietTypeId;

    private String specialInstruction;

    @NotNull(message = "Effective from date is required")
    private LocalDate effectiveFrom;

    @NotNull(message = "Ordered by user ID is required")
    @Positive(message = "Ordered by user ID must be an Valid User Id")
    private Long orderedBy;
}
