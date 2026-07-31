package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MasSurgeryPricingRequest {

    private Long surgeryId;
    private Long billingTypeId;
    private BigDecimal amount;
    private String discountAllowed;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}