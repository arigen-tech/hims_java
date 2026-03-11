package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class OpeningBalanceEntryHeaderResponse {

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
    private String balanceType;

}
