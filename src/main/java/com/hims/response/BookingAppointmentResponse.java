package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class BookingAppointmentResponse {
    private Long patientId;
    private Instant visitDate;
    private Instant startTime;
    private Instant EndTime;
    private Long tokenNo;
}
