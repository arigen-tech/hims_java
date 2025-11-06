package com.hims.response;

import lombok.Data;

import java.util.List;

@Data
public class ResultEntryInvestigationResponse {

    private Long investigationId;
    private Long investigationName;
    private String diagNo;
    private String unit;
    private String sampleName;
    private String normalValue;
    private String result;
    private Long resultEntryDetailsId;
    private List<ResultEntrySubInvestigationRes> resultEntrySubInvestigationResponses;
}
