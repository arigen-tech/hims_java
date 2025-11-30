package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class PaymentUpdateRequest {
    private String billingType;
    private int billHeaderId;
    private BigDecimal amount;
    private String  mode;
    private String  paymentReferenceNo;
    List<InvestigationandPackegBillStatus> investigationandPackegBillStatus;


}
