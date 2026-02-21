package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LabAmenedAuditReportResponse {

    private Long amendId;
    private String sampleId;
    private String patientName;
    private String investigationName;
    private  String unitName;
    private String oldResult;
    private String newResult;
    private String reasonForChange;
    private String authorizedBy;
    private LocalDateTime dateTime;
}
