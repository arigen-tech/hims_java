package com.hims.response;

import lombok.Data;

import java.time.Instant;

@Data
public class AppointmentBookingHistoryResponseDetails {

    private Long visitId;
    private Long patientId;
    private String patientName;
    private String mobileNumber;
    private String patientAge;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private Instant appointmentDate;
    private Instant appointmentStartTime;
    private Instant appointmentEndTime;
    private String status;
    private String reason;
    private boolean canReschedule;
    private boolean canCancel;
}