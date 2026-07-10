package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface OpeningBalanceEntryDetailProjection {

    Long getBalanceTId();
    Long getBalanceMId();
    Long getItemId();

    String getItemName();
    String getItemUnit();
    BigDecimal getItemGst();
    String getItemCode();

    String getBatchNo();
    LocalDate getManufactureDate();
    LocalDate getExpiryDate();

    Long getQty();
    Long getUnitsPerPack();

    BigDecimal getPurchaseRatePerUnit();
    BigDecimal getGstPercent();
    BigDecimal getMrpPerUnit();

    String getHsnCode();

    BigDecimal getBaseRatePerUnit();
    BigDecimal getGstAmountPerUnit();
    BigDecimal getTotalPurchaseCost();
    BigDecimal getTotalMrpValue();

    Long getBrandId();
    Long getManufacturerId();

    String getBrandName();
    String getManufacturerName();
}