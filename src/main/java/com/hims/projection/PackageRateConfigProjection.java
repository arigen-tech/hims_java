package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PackageRateConfigProjection {
    Long getConfigId();

    Long getPackageId();
    String getPackageName();

    Long getBillingTypeId();
    String getBillingTypeName();

    Long getInsuranceId();
    String getInsuranceName();

    Long getTpaId();
    String getTpaName();

    Long getCorporateId();
    String getCorporateName();

    Long getRoomCategoryId();
    String getRoomCategoryName();

    BigDecimal getAmount();
    LocalDate getEffectiveFrom();
    LocalDate getEffectiveTo();

    String getPreAuthRequired();

    BigDecimal getCopayPercent();
    BigDecimal getMaxClaimAmount();

    String getStatus();
}
