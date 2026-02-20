package com.hims.projection;

import java.time.Instant;
import java.time.LocalDate;

public interface CancelledAppointmentProjection {
    Long getVisitId();
    Long getPatientId();
    String getPatientName();
    String getMobileNumber();
    String getPatientAge();
    String getGender();
    Long getDoctorId();
    String getDoctorName();
    Long getDepartmentId();
    String getDepartmentName();
    LocalDate getAppointmentDate();
    String getAppointmentTime();  // Combined "HH:MM to HH:MM"
    Instant getCancellationDateTime();
    String getCancelledBy();
    String getCancellationReason();
}


