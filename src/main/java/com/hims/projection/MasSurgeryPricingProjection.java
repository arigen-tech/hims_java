package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MasSurgeryPricingProjection {
    Long getSurgeryPricingId();
    Long getSurgeryId();
    String getSurgeryName();
    BigDecimal getAmount();
    LocalDate getEffectiveFrom();
    LocalDate getEffectiveTo();
    String getRemarks();
    String getStatus();
    Long getBillingTypeId();
    String getBillingTypeName();
}