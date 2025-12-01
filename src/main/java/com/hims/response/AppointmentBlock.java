package com.hims.response;

import lombok.Data;

import java.time.Instant;

@Data
public class AppointmentBlock {
    private long billingHdId;
    private Long visitId;
    private String visitType;
    private Long tokenNo;
    private String department;
    private String consultedDoctor;
    private String sessionName;
    private Instant visitDate;
}

