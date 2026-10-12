package com.hims.projection;

import java.time.LocalDateTime;

public interface ActiveAdmissionOtProjection {

    Long getInpatientId();

    Long getVisitId();

    Long getPatientId();

    String getPatientName();

    String getUhid();

    String getAge();

    Long getGenderId();

    String getGender();

    String getMobileNo();

    String getAdmissionNo();

    Long getWardId();

    String getWard();

    Long getRooId();

    String getRoom();

    Long getBedId();

    String getBed();

    String getDiagnosis();

    LocalDateTime getAdmissionDateTime();

    String getDoctorName();
}
