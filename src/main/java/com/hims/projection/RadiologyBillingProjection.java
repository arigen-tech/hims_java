package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface RadiologyBillingProjection {
    Long getOrderId();
    String getRegistrationNo();
    String getPatientName();
    String getMobileNumber();
    LocalDate getAge();
    String getGenderName();
    Long getBillingHeaderId();
    Double getNetAmount();
    Long getPatientId();
    String getAppointmentDate();
    String getServiceCategoryName();
    String getAppointmentDateForRadiology();

}
