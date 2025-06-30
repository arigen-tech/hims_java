package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
@Data
public class OpeningBalanceEntryResponse {
    private Long balanceMId;
    private String balanceNo;
    private Long hospitalId;
    private Long departmentId;
    private String departmentName;
    private String enteredBy;
    private String remarks;
    private String status;
    private LocalDateTime enteredDt;
    private String approvedBy;
    private LocalDateTime approvalDt;
    private LocalDateTime lastUpdatedDt;
    private List<OpeningBalanceDtResponse> openingBalanceDtResponseList;
}
