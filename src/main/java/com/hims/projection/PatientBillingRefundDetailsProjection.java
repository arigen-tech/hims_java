package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface PatientBillingRefundDetailsProjection {

    String getRefundStatus();

    BigDecimal getRefundAmount();

    String getRefundMode();

    String getTransactionNumber();

    LocalDateTime getRefundDate();

    String getProcessedBy();
}