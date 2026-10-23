package com.hims.projection;

import java.time.LocalDateTime;

public interface PatientVisitQueueProjection {

     Long getVisitId();
     Long getPatientId();
     LocalDateTime getAppointmentStartTime();
     LocalDateTime getAppointmentEndTime();
     String getVisitStatus();
     String getPatientName();
     String getAge();
     String getGender();
     String getMobileNumber();
}
