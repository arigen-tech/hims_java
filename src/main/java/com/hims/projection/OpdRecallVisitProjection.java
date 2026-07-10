package com.hims.projection;

import java.time.LocalDate;

public interface OpdRecallVisitProjection {

    Long getVisitId();

    Long getPatientId();

    Long getHospitalId();

    String getPatientName();

    String getMobileNo();

    String getGender();

    String getRelation();

    LocalDate getDob();

    String getAge();

    Long getDeptId();

    String getDeptName();

    Long getDoctorId();

    String getDoctorName();
}