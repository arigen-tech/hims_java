package com.hims.projection;

import java.time.LocalDate;

public interface OpdPreConsultationProjection {
    Long getVisitId();
    Long getPatientId();
    String getPatientName();
    String getPatientAge();
    String getGender();
    String getDepartmentName();
    Long getDepartmentId();
    String getMobileNumber();
    String getVisitType();
    String getDoctorName();
    String getDoctorId();
    LocalDate getAppointmentDate();
    String getAppointmentTime(); // Combined "HH:MM to HH:MM"
    String getTokenNumber();
}
