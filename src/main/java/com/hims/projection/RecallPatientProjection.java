package com.hims.projection;

import java.time.LocalDate;

public interface RecallPatientProjection {

    Long getPatientId();

    Long getVisitId();

    String getPatientFn();

    String getPatientMn();

    String getPatientLn();

    String getPatientMobileNumber();

    LocalDate getPatientDob();

    String getPatientAge();

    String getGenderName();

    String getRelationName();

    Long getDepartmentId();

    String getDepartmentName();

    Long getDoctorId();

    String getDoctorFirstName();

    String getDoctorMiddleName();

    String getDoctorLastName();

    Long getHospitalId();
}