package com.hims.projection;

import java.time.Instant;
import java.time.LocalDate;

public interface OpdPatientDetailsWaitingProjection {

    String getPatientName();
    String getTokenNo();
    String getMobileNo();
    String getEmployeeNo();
    String getGender();
    String getRelation();
    LocalDate getDob();
    String getAge();
    Long getDeptId();
    String getDeptName();
    Long getDocterId();
    String getDocterName();
    Long getVisitId();
    Long getPatientId();
    Long getHospitalId();
    Long getSessionId();
    String getSessionName();
    String getDisplayPatientStatus();
    Instant getVisitDate();
}