package com.hims.projection;

import java.time.LocalDate;

public interface OpdBillingProjection {
    Long getPatientId();
    Long getVisitId();
    String getRegistrationNo();
    String getMobileNo();
    String getAppointmentDate();
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
