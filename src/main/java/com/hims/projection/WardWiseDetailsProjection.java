package com.hims.projection;

import java.time.LocalDate;

public interface WardWiseDetailsProjection {

    Long getPatientId();

    Long getIpdPatientId();

    String getPatientName();

    Long getRoomId();
    String getRoomName();
    Long getBedId();
    String getBedNumber();

    LocalDate getAdmitDate();

    Long getDays();

    String  getAdmissionNo();

    String getAdmissionStatus();

    String getIpdInternalStatus();

    String getAge();

    String getGender();
    String getDoctor();
    Long getDiagnosisId();
    String getDiagnosisType();
    String getDiagnosis();
    Long getDoctorId();



}