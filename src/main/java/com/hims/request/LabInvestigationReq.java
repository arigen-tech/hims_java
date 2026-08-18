package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
public class LabInvestigationReq {
    private Long id;
    private LocalDate appointmentDate;
   // private boolean checkStatus;
    private BigDecimal actualAmount;
    private BigDecimal discountedAmount;
    private String type;


}
