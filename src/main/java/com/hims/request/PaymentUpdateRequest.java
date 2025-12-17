package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PaymentUpdateRequest {

    private String billingType;
    private List<Integer> billHeaderIds;
    private int billHeaderId;
    private BigDecimal amount;
    private String mode;
    private String paymentReferenceNo;
    List<InvestigationandPackegBillStatus> investigationandPackegBillStatus;
    private BigDecimal registrationCost;
    private List<OpdBillPayment> opdBillPayments;

    @Getter
    @Setter
    public static class OpdBillPayment {
        private Integer billHeaderId;
        private BigDecimal netAmount;
    }
}

