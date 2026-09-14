package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface BillingHeaderResponseProjection {
    Long getHeaderId();
    Long getVisitId();

    Long getPrescriptionHeaderId();
    String getBillNo();
    String getPatientName();
    String getPhoneNo();
    String getAge();
    String getRelation();
    String getSex();
    String getDepartment();
    LocalDateTime getBillDate();
    BigDecimal getNetAmount();
    Long getServiceCategoryId();
    String getServiceCategoryName();
    String getPaymentStatus();
    String getRegistrationNo();
}