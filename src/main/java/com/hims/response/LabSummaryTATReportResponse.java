package com.hims.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabSummaryTATReportResponse {

    private Long investigationId;
    private String investigationName;
    private Integer expectedTatHours;
    private Integer totalTests;
    private Long averageTatHours;
    private Long minTatHours;
    private Long maxTatHours;
    private Long noOfTestsWithinTatHour;
    private Long noOfTestsBreached;
    private Integer compliance;

}
