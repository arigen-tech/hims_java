package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentUpdateRequest {
    private int billHeaderId;
    private Long amount;
    private String  mode;
    private String  paymentReferenceNo;


}
