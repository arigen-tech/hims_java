package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class ResultValidationUpdateRequest {
    private Long resultEntryHeaderId;
    private List<ResultEntryValidationRequest> validationList;
}
