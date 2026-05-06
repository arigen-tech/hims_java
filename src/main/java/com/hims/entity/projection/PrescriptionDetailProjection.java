package com.hims.entity.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Projection interface for prescription details within 30 days
 * Returns only the necessary fields for prescription details query
 */
public interface PrescriptionDetailProjection {

    Long getPrescriptionDtId();

    Long getPrescriptionHdId();

    Long getItemId();

    String getDosage();

    String getFrequency();

    Integer getDays();

    BigDecimal getTotal();

    BigDecimal getIssuedQty();

    String getRoute();

    String getInstruction();

    BigDecimal getUnitPrice();

    BigDecimal getDiscount();

    BigDecimal getGstRate();

    BigDecimal getLineCost();

    String getStatus();

    String getBatchNo();

    LocalDate getExpiryDate();
}

