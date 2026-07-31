package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
