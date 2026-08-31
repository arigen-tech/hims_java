package com.hims.projection;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface PendingForOtProjection {

    Long getOtBookingRequestId();

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

    Long getSurgeonId();

    String getSurgeonName();

    String getPatientType();

    Long getOtId();

    String getOtName();

    LocalDate getRequestedDate();

    LocalTime getRequestedTime();

    String getRequestedBy();

    String getRequestedNo();

}