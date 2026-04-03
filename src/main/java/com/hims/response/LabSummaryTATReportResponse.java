package com.hims.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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

    public LabSummaryTATReportResponse(
            Long investigationId,
            String investigationName,
            Integer expectedTatHours,
            Long totalTests,
            Double avgTat,
            Long minTat,
            Long maxTat,
            Long withinTat,
            Long breached
    ) {
        this.investigationId = investigationId;
        this.investigationName = investigationName;
        this.expectedTatHours = expectedTatHours;
        this.totalTests = totalTests.intValue();
        this.averageTatHours = avgTat != null ? avgTat.longValue() : 0;
        this.minTatHours = minTat;
        this.maxTatHours = maxTat;
        this.noOfTestsWithinTatHour = withinTat;
        this.noOfTestsBreached = breached;
        this.compliance = totalTests == 0 ? 0 :
                (int) ((withinTat * 100) / totalTests);
    }

}
