package com.hims.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OpdRecallVisitResponse {
    private Long visitId;
    private Long patientId;
    private String patientName;
    private String mobileNo;
    private String gender;
    private String age;
    private String deptName;
    private String doctorName;


}
