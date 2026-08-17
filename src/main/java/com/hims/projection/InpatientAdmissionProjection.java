package com.hims.projection;

import java.time.LocalDate;
import java.time.LocalTime;

public interface InpatientAdmissionProjection {

    // Patient Information
    Long getPatientId();
    String getPatientFn();
    String getPatientMn();
    String getPatientLn();
    String getUhidNo();
    String getPatientAge();
    Long getGenderId();
    String getGenderName();
    String getPatientMobileNumber();
    String getEmergencyContactNo();

    // Admission Information
    String getAdmissionNo();
    LocalDate getAdmissionDate();
    LocalTime getAdmissionTime();
    String getAdmissionCategoryName();
    String getAdmissionTypeName();
    String getAdmissionSourceName();
    String getAdmissionStatusName();
    LocalDate getDischargeDate();

    // Doctor & Location
    String getDoctorName();
    String getWardName();
    String getRoomName();
    String getBedName();
    String getCareLevelName();

    // Clinical Info
    String getConditionNotes();
    String getInitialDiagnosis();
    String getIcdName();
    String getPatientConditionName();
    String getAdmissionPriority();
}