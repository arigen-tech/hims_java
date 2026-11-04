package com.hims.projection;

import java.time.LocalDate;

public interface OtDashboardProjection {
    LocalDate getScheduledDate();
    Long getBookingStatusId();
    String getStatusName();
    Long getBookingCount();
    String getOtNames();
}
