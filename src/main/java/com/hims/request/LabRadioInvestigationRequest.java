package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class LabRadioInvestigationRequest {
    private Long id;
    private LocalDate appointmentDate;
    private Boolean checkStatus;
    private BigDecimal actualAmount;
    private BigDecimal discountedAmount;
    private String type;
    private String remarks;

}
