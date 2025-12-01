package com.hims.response;

import lombok.Data;
import java.util.List;

@Data
public class PaymentResponse {

    private String billNo;
    private String msg;
    private String paymentStatus;

    private List<OpdPaymentItem> billPayments;
}
