package com.hims.projection;

import java.time.LocalDate;
import java.time.LocalTime;

public interface PacPendingProjection {
    Long getOtBookingId();
    String getBookingNo();
    Long getOtBookingRequestId();
    Long getPatientId();
    String getPatientName();
    String getUhidNo();
    String getIpNo();
    String getPatientType();
    String getDepartmentName();
    String getPrimarySurgeonName();
    String getDiagnosis();
    LocalDate getScheduledDate();
    LocalTime getScheduledStartTime();
    LocalTime getScheduledEndTime();
    String getOtName();
    String getWardName();
    String getBedNumber();
    String getSurgeries();
}
