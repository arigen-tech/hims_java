package com.hims.request;

import lombok.Data;

@Data
public class ResultUpdateDetailRequest {
    private Long resultEntryDetailsId;
    private String oldResult;
    private Long amendmentTypeId;
    private String result;
    private String remarks;
    private Long fixedId;
    private String comparisonType;
}
