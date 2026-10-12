package com.hims.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefundDetailsResponse {

    private Long refundId;
    private String gatewayRefundId;
    private String refundNumber;
    private BigDecimal refundAmount;
    private String refundStatus;
    private String refundReason;
    private BigDecimal paymentAmount;
    private LocalDateTime initiatedOn;
    private String gatewayPaymentId;
    private String paymentMode;
    private String paymentVia;
    private String gatewayReferenceType;
    private String gatewayReferenceNo;


}
