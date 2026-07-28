package com.hims.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Builder
@Data
public class DailyCaseSheetEntryResponse {
    private Long caseSheetEntryId;
    private Long inpatient;
    private String notes;
    private String investigation;
    private String medicines;
    private String procedure;
    private String plan;
    private String followUp;
    private LocalDateTime visitDateTime;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private Long visitTypeId;
    private String visitTypeName;





}
