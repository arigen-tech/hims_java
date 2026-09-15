package com.hims.response;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Data

public class VisitResponse {
    private Long id;
    private Long tokenNo;
    private LocalDateTime visitDate;
    private LocalDateTime lastChgDate;
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
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String preConsultation;
    private Long billingHdId;
}
