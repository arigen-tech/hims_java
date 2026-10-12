package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VisitRequest {
    private Long id;
    private Long tokenNo;
    private LocalDateTime tokenStartTime;
    private LocalDateTime tokenEndTime;
    private LocalDateTime visitDate;
    private Long priority;
    private Long departmentId;
    private Long doctorId;
    private String doctorName;
    private Long patientId;
    private Long hospitalId;
    private Long iniDoctorId;
    private Long sessionId;
    private String billingStatus;
    private String visitType;
}
