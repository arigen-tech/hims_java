package com.hims.response;

import lombok.Data;

import java.time.Instant;

@Data
public class RescheduleAppointmentResponse {
    private Long visitId;
    private String message;
}
