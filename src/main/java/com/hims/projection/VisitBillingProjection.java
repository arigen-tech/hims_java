package com.hims.projection;

import java.math.BigDecimal;
import java.time.Instant;

public interface VisitBillingProjection  {

    Long getVisitId();
    Long getBillingHdId();
    Instant getVisitDate();
    String getConsultedDoctor();
    String getDepartmentName();
    String getSessionName();
    String getVisitType();
    Long getTokenNo();
    BigDecimal getTariff();
    BigDecimal getRegistrationCost();
    BigDecimal getTaxPercent();
    BigDecimal getDiscountAmount();
    BigDecimal getTotalAmount();
    BigDecimal getTaxAmount();
    BigDecimal getNetAmount();

    String getPolicyCode();
    String getPolicyType();
    Integer getPolicyEligibilityDays();
    BigDecimal getPolicyDiscountPercent();
    String getPolicyDescription();


}
