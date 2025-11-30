package com.hims.response;

import lombok.Data;

import java.util.List;

@Data
public class ResultEntryInvestigationResponse {
    private Long resultEntryDetailsId;
    private Long investigationId;
    private String investigationName;
    private String diagNo;
    private String unit;
    private String sampleName;
    private String remarks;
    private String result;
    private String normalValue;
    private Boolean inRange;
    List<ResultEntrySubInvestigationRes> resultEntrySubInvestigationRes;

}
