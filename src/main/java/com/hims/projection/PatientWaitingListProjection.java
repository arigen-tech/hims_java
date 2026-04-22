package com.hims.projection;

public interface PatientWaitingListProjection {

    Long getVisitId();
    Long getPatientId();
    String getPatientName();
    Integer getPatientAge();
    String getMobileNumber();
    String getVisitType();
    Long getDoctorId();
    String getDoctorName();
    String getTokenNo();
    String getRelation();
    String getGender();
    String getDob();
}
