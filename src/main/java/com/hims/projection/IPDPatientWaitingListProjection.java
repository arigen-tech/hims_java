package com.hims.projection;

import java.time.LocalDate;

public interface IPDPatientWaitingListProjection {

    Long getOpdPatientDetailsId();
    Long getVisitId();
    Long getPatientId();
    String getUhid();
    String getPatientName();
    String getPatientMobileNo();
    String getAge();
    String getGender();
    LocalDate getAdmissionAdviseDate();
    String getDoctorName();
    Long getDepartmentId();
    String getDepartment();
    Long getWardId();
    String getWardName();
    String getBed();
    String getAdmissionSource();
    Long getCareLevelId();
    String getCareLevel();
    Long getAdmissionWardCategoryId();
    String getAdmissionWardCategoryName();
}