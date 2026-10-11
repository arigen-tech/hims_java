package com.hims.projection;

import java.time.LocalDateTime;

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
    String getNisNo();
    LocalDateTime getVisitDateTime();
    Long getPrescriptionHdId();
    String getPrescriptionStatus();
}
