package com.hims.response;

import lombok.Data;

@Data
public class PaymentResponse {
    private String billNo;
    private String msg;
    private String paymentStatus;

}
