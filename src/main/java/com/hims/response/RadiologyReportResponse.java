package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class RadiologyReportResponse {
    private Long radStudyReportId;
    private Long radOrderDtId;
    private String accessionNo;
    private String uhidNo;
    private String patientName;
    private String investigationName;
    private String modality;
    private String reportDesc;
    private String reportImagePath;
    private String reportStatus;
    private LocalDate reportDate;
    private Long createdBy;
    private LocalDateTime createdOn;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
}
