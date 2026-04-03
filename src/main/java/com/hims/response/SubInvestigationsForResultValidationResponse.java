package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SubInvestigationsForResultValidationResponse {



                private Long subInvestigationId;
                private String subInvestigationName;
                private String normalValue;
                private String comparisonType;
                private String unitName;
                private String resultType;
                private Long fixedId;
                private String generatedSampleId;
                private String result;
                private String remarks;



}
