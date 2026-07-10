package com.hims.request;

import lombok.Data;

import java.time.Instant;

@Data
public class RescheduleAppointmentRequest {
    private Long visitId;
    private Instant visitDate;
    private Instant appointmentStartTime;
    private Instant appointmentEndTime;
    private Long tokenNumber;
}
