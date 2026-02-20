package com.hims.response;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class CancelledAppointmentResponse {
    private Long visitId;
    private Long patientId;
    private String patientName;
    private String mobileNumber;
    private String age;
    private String gender;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private LocalDate appointmentDate;
    private String appointmentTime;
    private Instant cancellationDateTime;
    private String cancelledBy;
    private String cancellationReason;
}


