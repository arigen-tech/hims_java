package com.hims.response;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class OpdPatientDetailsWaitingresponce {

    private String patientName;
    private String tokenNo;
    private String mobileNo;
    private String employeeNo;
    private String gender;
    private String relation;
    private LocalDate dob;
    private String age;
    private Long deptId;
    private String deptName;
    private Long docterId;
    private String docterName;
    private Long visitId;
    private Long patientId;
    private Long hospitalId;
    private Long sessionId;
    private String sessionName;
    private String displayPatientStatus;
    private Instant visitDate;
}
