package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ResultEntryUpdateHeaderResponse {
    private Long resultEntryHeaderId;
     List<ResultEntryUpdateInvestigationResponse> resultEntryUpdateInvestigationResponseList;
}
