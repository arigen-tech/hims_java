package com.hims.projection;

import java.time.LocalDate;

public interface LabBillingProjection {
    Long getOrderId();
    String getRegistrationNo();
    String getPatientName();
    String getMobileNumber();
    String getRelationName();
    LocalDate getAge();
    String getGenderName();
    Long getBillingHeaderId();
    Double getNetAmount();
    Long getPatientId();
    String getDepartmentName();
    String getAppointmentDate();
    String getServiceCategoryName();
    String getOrderDate();

}
