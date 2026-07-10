package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SubInvestigationResultResponse {

    private Long subInvestigationId;
    private String subInvestigationName;
    private String normalValue;
    private Long normalId;
    private String comparisonType;
    private String unitName;
    private String resultType;
}
