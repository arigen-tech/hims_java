package com.hims.response;

import com.hims.entity.BillingHeader;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpdBillingPaymentResponse {
    BillingHeader header;
    boolean paymentFlag;
}
