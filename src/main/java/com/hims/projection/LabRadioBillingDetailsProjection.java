package com.hims.projection;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public interface LabRadioBillingDetailsProjection {

    Long getVisitId();
    Long getBillinghdid();
    Long getPatientid();
    Long getBillingdtId();

    String getFirstName();
    String getMiddleName();
    String getLastName();

    String getMobileNo();
    LocalDate getDob();
    String getGender();

    BigDecimal getBillHdTotalAmount();
    String getBillingStatus();
    Instant getVisitDate();

    String getUhidNo();
    String getRelation();
    String getBillingType();
    String getDepartment();
    String getAddress();
    Integer getOrderhdid();
    String getOrderhdPaymentStatus();
    Long getTokenNo();
    String getItemName();
    Integer getQuantity();
    BigDecimal getBasePrice();
    BigDecimal getTariff();
    BigDecimal getDiscount();
    BigDecimal getAmountAfterDiscount();
    BigDecimal getTaxPercent();
    BigDecimal getTaxAmount();
    BigDecimal getNetAmount();
    String getDetailPaymentStatus();
    Long getInvestigationId();
    String getInvestigationName();
    Long getPackageId();
    String getPackageName();
}
