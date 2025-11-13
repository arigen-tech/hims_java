package com.hims.request;

import lombok.Data;

import java.util.List;
@Data
public class ResultUpdateRequest {
    private Long resultEntryHeaderId;
    private List<ResultUpdateDetailRequest> resultUpdateDetailRequests;
}
