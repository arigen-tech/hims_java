package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class MasProcedurePricingRequest {
    private Long procedureId;
    private BigDecimal basePrice;
    private String discountAllowed;
    private BigDecimal discount;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private Long billingTypeId;
}
