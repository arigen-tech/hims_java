package com.hims.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class OtBookingRescheduleRequest {
    @NotNull @Positive
    private Long otBookingId;
    @NotNull @Positive
    private Long newOtId;
    @NotNull
    private LocalDate newScheduledDate;
    @NotNull
    private LocalTime newStartTime;
    @NotNull
    private LocalTime newEndTime;
    @NotNull @Positive
    private Long reasonId;
    @Size(max = 1000)
    private String remarks;
}
