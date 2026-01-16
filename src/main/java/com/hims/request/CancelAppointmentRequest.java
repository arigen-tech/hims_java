package com.hims.request;

import lombok.Data;

import java.time.Instant;

@Data
public class CancelAppointmentRequest {
    private Long visitId;
    private Long cancelReasonId;
}
