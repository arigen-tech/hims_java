package com.hims.response;

import com.hims.entity.DgFixedValue;
import lombok.Data;

import java.util.List;

@Data
public class ResultSubInvestigationResponse {
    private Long subInvestigationId;
    private String subInvestigationName;
    private Long sampleId;
    private String sampleName;
    private String unit;
    private String normalValue;
    private Long normalId;
     private List<DgFixedValueResponse> dgFixedValueResponseList;
    private String comparisonType;
    private String resultType;

}
