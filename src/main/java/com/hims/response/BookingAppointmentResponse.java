package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingAppointmentResponse {
    private Long patientId;
    private LocalDateTime visitDate;
    private LocalDateTime startTime;
    private LocalDateTime EndTime;
    private Long tokenNo;
}
