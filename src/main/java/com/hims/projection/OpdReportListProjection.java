package com.hims.projection;

public interface OpdReportListProjection {
    Long getVisitId();
    Long getPatientId();
    String getPatientName();
    String getMobileNumber();
    String getUhid();
    String getRelation();
    String getGender();
    String getAge();
    String getSpecialty();
    String getDoctorName();
    String getVisitDateTime();
    Long getPrescriptionHdId();
    String getPrescriptionStatus();
}
