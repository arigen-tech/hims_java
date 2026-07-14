package com.hims.projection;

import java.time.LocalDate;

public interface WardWiseDetailsProjection {

    Long getPatientId();

    Long getIpdPatientId();

    String getPatientName();

    String getWardName();

    String getRoomName();

    String getBedNumber();

    LocalDate getAdmitDate();

    Long getDays();

    Long getBedCount();

    String  getAdmissionNo();

    String getAdmissionStatus();

    String getIpdInternalStatus();


}