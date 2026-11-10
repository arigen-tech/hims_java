package com.hims.projection;

import java.time.LocalDate;
import java.time.LocalTime;

public interface OtBookingScheduleProjection {
    Long getOtBookingId();
    Long getOtBookingRequestId();
    Long getOtId();
    LocalDate getScheduledDate();
    LocalTime getScheduledStartTime();
    LocalTime getScheduledEndTime();
    Long getBookingStatusId();
    String getStatusCode();
}
