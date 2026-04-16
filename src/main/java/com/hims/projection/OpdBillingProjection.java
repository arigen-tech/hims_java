package com.hims.projection;

import java.time.Instant;
import java.time.LocalDate;

public interface OpdBillingProjection {
    Long getPatientId();
    Long getVisitId();
    String getRegistrationNo();
    String getMobileNo();
    Instant getAppointmentDate();
    Long getBillingHdId();
    String getPatientName();
    LocalDate getAge();
    String getGender();
    String getRelation();
    String getBillingType();
    String getConsultingDoctorName();
    String getDepartmentName();
    Double getNetAmount();
}
