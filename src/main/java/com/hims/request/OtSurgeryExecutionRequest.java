package com.hims.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OtSurgeryExecutionRequest {
    @NotNull @Positive
    private Long otBookingId;

    @NotNull @Positive
    private Long otBookingRequestDtId;

    private LocalDateTime surgeryStartDatetime;
    private LocalDateTime surgeryEndDatetime;
    private String executionStatus;
    private String signoutRemarks;
    private String signoutVerifiedBy;
    private LocalDateTime signoutVerifiedDatetime;
    private String executionRemarks;
}
