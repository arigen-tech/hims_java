package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class CancelAppointmentRequest {
    private Long visitId;
    private Long cancelReasonId;
    public String paymentMode;
    private BigDecimal refundAmount;
}
