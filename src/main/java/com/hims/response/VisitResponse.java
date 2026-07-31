package com.hims.response;

import lombok.Data;

import java.time.Instant;

@Data

public class VisitResponse {
    private Long id;
    private Long tokenNo;
    private Instant visitDate;
    private Instant lastChgDate;
    private String visitStatus;
    private Long priority;
    private Long departmentId;
    private String departmentName;
//    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private Long hospitalId;
    private String hospitalName;
    private Long iniDoctor;
    private Long sessionId;
    private String billingStatus;
    private Instant startTime;
    private Instant endTime;
    private String preConsultation;
    private Long billingHdId;
}
