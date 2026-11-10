package com.hims.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OtBookingCancelRequest {
    @NotNull @Positive
    private Long otBookingId;
    @NotNull @Positive
    private Long reasonId;
    @Size(max = 1000)
    private String remarks;
}
