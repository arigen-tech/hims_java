package com.hims.request;

import lombok.Data;

import java.util.List;
@Data
public class ResultUpdateRequest {
    private Long orderHdId;
    private List<ResultUpdateDetailRequest> resultUpdateDetailRequests;
}
