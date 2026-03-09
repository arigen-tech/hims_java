package com.hims.projection;

import java.math.BigDecimal;

public interface BillingHeaderResponseProjection {
    Long getHeaderId();
    Long getVisitId();
    String getBillNo();
    String getPatientName();
    String getPhoneNo();
    String getAge();
    String getRelation();
    String getSex();
    String getDepartment();
    String getBillDate();
    BigDecimal getNetAmount();
    Long getServiceCategoryId();
    String getServiceCategoryName();
    String getPaymentStatus();
    String getRegistrationNo();
}