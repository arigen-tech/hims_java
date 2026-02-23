package com.hims.projection;

import java.time.Instant;

public interface AppSetupProjection {
    String getFromTime();
    String getToTime();

    Long getHospitalId();
    Long getDeptId();

    Instant getValidFrom();
    Instant getValidTo();
    Integer getDayOfWeek();

    Long getDoctorId();
    Long getSessionId();

    String getStartTime();
    String getEndTime();
    Integer getTimeTaken();

    // days row fields
    Long getId();
    String getDays();
    Integer getMaxNoOfDays();
    Integer getMinNoOfDays();
    Integer getTotalToken();
    Integer getTotalInterval();
    Integer getStartToken();
    Integer getTotalOnlineToken();
    String getOpdLocation();
}
