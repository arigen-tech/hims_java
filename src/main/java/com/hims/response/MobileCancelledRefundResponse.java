package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MobileCancelledRefundResponse {
    private Long visitId;
    private Long patientId;
    private Long billingHeaderId;
    private String patientName;
    private String mobileNumber;
    private String age;
    private String gender;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private Instant cancellationDateTime;
    private String cancelledBy;
    private String cancellationReason;
    private Long billingAmount;
    private Long paymentId;
    private Long refundId;
    private BigDecimal refundAmount;
    private String refundReferenceNo;
    private String refundReason;
    private String gatewayRefundId;
    private LocalDateTime refundDate;
    private String refundStatus;
    private Long paymentModeId;
    private String paymentModeCode;
    private String paymentModeName;
}
