package com.hims.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class OpeningBalanceEntryRequest {
    private Long departmentId;
    private String enteredBy;
    private LocalDateTime enteredDt;
    private List<OpeningBalanceDtRequest> storeBalanceDtList;
}
