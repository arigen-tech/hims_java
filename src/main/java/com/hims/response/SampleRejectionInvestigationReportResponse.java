package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class SampleRejectionInvestigationReportResponse {

    private String orderNo;
    private LocalDate orderDate;
    private String patientName;
    private String age;
    private String gender;
    private String mobileNum;
    private String investigationName;
    private String sampleId;
    private String orderStatus;
    private String rejectionReason;
    private String modalityName;
}
