package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureWorklistResponse {

    private Long procedureHdId;
    private Long procedureDtId;
    private Long patientId;

    private String mobileNo;
    private String patientName;

    private Integer age;
    private String gender;

    private String department;
    private String procedure;

    private Integer completedSessions;
    private Integer totalSessions;

    private LocalDateTime scheduledDateTime;

    private String advisedBy;
    private String billingStatus;
}