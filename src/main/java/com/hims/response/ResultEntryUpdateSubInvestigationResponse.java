package com.hims.response;

import lombok.Data;

import java.util.List;
@Data
public class ResultEntryUpdateSubInvestigationResponse {
    private Long resultEntryDetailsId;
    private Long subInvestigationId;
    private String subInvestigationName;
    private String sampleName;
    private String unit;
    private String normalValue;
    private String result;
    private String remarks;
    private String comparisonType;
    private Long fixedId;
    private Boolean inRange;
    private List<DgFixedValueResponse> fixedDropdownValues;
}
