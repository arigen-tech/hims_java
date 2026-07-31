package com.hims.projection;

import java.time.LocalDate;

public interface PaidCancelledAppointmentProjection {

    Long getVisitId();
    Long getPatientId();
    Long getBillingHeaderId();
    String getRegistrationNo();
    String getPatientName();
    String getMobileNo();
    String getAge();
    String getGender();
    String getBillingType();
    LocalDate getDate();
    Long getBillingAmount();
    LocalDate getCancelledDate();
    LocalDate getRefundDate();
    String getRefundStatus();
    String getDepartmentName();
}