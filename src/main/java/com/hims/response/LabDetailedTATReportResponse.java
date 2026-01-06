package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LabDetailedTATReportResponse {

    private  Long tatId;
    private Long orderId;
    private String investigationName;
    private  String generatedSampleId;
    private LocalDateTime sampleReceivedDate;
    private LocalDateTime reportAuthorizedDate;
    private Integer expectedTatHours;
    private Long actualTatHours;
    private Long delay;
    private String tatStatus;
    private String technicianName;

}
