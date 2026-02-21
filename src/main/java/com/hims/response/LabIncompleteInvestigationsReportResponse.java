package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LabIncompleteInvestigationsReportResponse {

    private String orderNo;
    private LocalDate orderDate;
    private String patientName;
    private String mobileNum;
    private String age;
    private String gender;
    private String sampleId;
    private String investigationName;
    private String currentStatus;
}
