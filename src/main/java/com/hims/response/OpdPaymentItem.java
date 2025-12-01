package com.hims.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OpdPaymentItem {
    private Integer billHeaderId;
    private Long visitId;
    private BigDecimal netAmount;  // Amount paid for this appointment
}
