package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SampleDetailsForValidationResponse {

    private Long sampleDetailsId;
    private Long investigationId;
    private  String generatedSampleId;
    private String investigationName;
    private Long sampleId;
    private String sampleName;
    private String quantity;
    private Long containerId;
    private String containerName;
    private String empanelledLab;
    private LocalDateTime sampleCollectedDatetime;
    private String rejectedReason;
    private String remarks;

}
