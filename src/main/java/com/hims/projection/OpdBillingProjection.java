package com.hims.projection;

public interface OpdBillingProjection {
    Long getPatientId();
    Long getVisitId();
    String getRegistrationNo();
    String getMobileNo();
    String getAppointmentDate();
    Long getBillingHdId();
    String getPatientName();
    String getAge();
    String getGender();
    String getRelation();
    String getBillingType();
    String getConsultingDoctorName();
    String getDepartmentName();
    Double getNetAmount();
}
