package com.hims.request;

import lombok.Data;

@Data
public class ResultEntryValidationRequest {

    private Long resultEntryDetailsId;
    private String result;
    private String remarks;
    private Boolean validated;  // true = validated, false/null = pending
    private Long fixedId;
    private String comparisonType;
}
