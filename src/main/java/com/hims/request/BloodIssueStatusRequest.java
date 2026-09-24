package com.hims.request;

import lombok.Data;

@Data
public class BloodIssueStatusRequest {
    private Long requestDtId;
    private Long inventoryId;
    private Boolean isIssued;
    private Boolean isRejected;
    private String rejectedReason;
}