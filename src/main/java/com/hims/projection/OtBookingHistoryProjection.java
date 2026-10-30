package com.hims.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface OtBookingHistoryProjection {
    Long getId();
    Long getHeaderid();
    Long getPatientId();
    String getUhidNo();
    String getPatientName();
    Long getDepartmentId();
    String getDepartmentName();
    LocalDate getPreferredDate();
    LocalTime getPreferredStartTime();
    LocalTime getPreferredEndTime();
    Long getOtId();
    String getOtName();
    String getPacRequired();
    String getPacDone();
    LocalDateTime getBookedDate();
    String getBookingNo();
    Long getPrimarySurgeonId();
    String getSurgeonName();
    LocalDateTime getCancelledDate();
    String getCancelledBy();
    Long getBookingStatusId();
    String getStatusName();
}
