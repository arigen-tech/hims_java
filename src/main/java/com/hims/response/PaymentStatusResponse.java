package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentStatusResponse {
    private Long InpatientId;
    private Long BillingHeaderId;
    private Long billStatusId;
    private String billStatus;
    private Long paymentStatusId;
    private String paymentStatus;
    private BigDecimal outstandingAmount;
}
