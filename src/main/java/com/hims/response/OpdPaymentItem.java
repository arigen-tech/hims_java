package com.hims.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OpdPaymentItem {
    private Integer billHeaderId;
    private Long visitId;
    private BigDecimal netAmount;
    private String patientName;
    private Long tokenNo;
    private String doctorName;// Amount paid for this appointment
}
