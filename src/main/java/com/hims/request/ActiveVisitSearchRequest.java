package com.hims.request;

import lombok.Data;

@Data
public class ActiveVisitSearchRequest {
    private Long doctorId;
    private Long sessionId;
    private String employeeNo;
    private String patientName; // user can type any name part
}
