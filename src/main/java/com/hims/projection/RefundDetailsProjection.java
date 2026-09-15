package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface RefundDetailsProjection {

    Long getRefundId();

    String getGatewayRefundId();

    String getRefundNumber();

    BigDecimal getRefundAmount();

    String getRefundStatus();

    String getRefundReason();

    BigDecimal getPaymentAmount();

    LocalDateTime getInitiatedOn();

    String getGatewayPaymentId();

    String getPaymentMode();

    String getPaymentVia();
    String getGatewayReferenceType();
    String getGatewayReferenceNo();
}