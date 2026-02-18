package com.hims.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class VisitRequest {
    private Long id;
    private Long tokenNo;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant visitDate;
//    private String visitStatus;
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
