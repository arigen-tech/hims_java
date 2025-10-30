package com.hims.response;

import lombok.Data;

@Data
public class ResultSubInvestigationResponse {
    private Long subInvestigationId;
    private String subInvestigationName;
    private Long sampleId;
    private String sampleName;
    private String unit;
    private String normalValue;
    private Long normalId;
    private String fixedValue;
    private Long fixedId;
    private String comparisonType;
    private String resultType;

}
