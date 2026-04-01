package com.hims.projection;

import java.time.Instant;

public interface AppointmentProjection {
    Long getAppointmentId();
    Long getSpecialityId();
    String getSpecialityName();
    Long getDoctorId();
    String getDoctorName();
    Long getSessionId();
    String getSessionName();
    Instant getVisitDate();
    String getVisitType();
    Long getTokenNo();
    String getVisitStatus();
    Instant getStartTime();
    Instant getEndTime();
}
