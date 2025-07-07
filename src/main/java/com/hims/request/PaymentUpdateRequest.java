package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class PaymentUpdateRequest {
    private int billHeaderId;
    private BigDecimal amount;
    private String  mode;
    private String  paymentReferenceNo;


}
