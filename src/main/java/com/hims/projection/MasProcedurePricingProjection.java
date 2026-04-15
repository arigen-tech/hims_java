package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MasProcedurePricingProjection {

    Long getProcedurePricingId();
    Long getProcedureId();
    String getProcedureName();
    BigDecimal getBasePrice();
    String getDiscountAllowed();
    LocalDate getEffectiveFrom();
    LocalDate getEffectiveTo();
    String getStatus();
    Long getBillingTypeId();
    String getBillingTypeName();
    BigDecimal getDiscount();
}