package com.hims.request;

import lombok.Data;

@Data
public class BloodIssueStatusRequest {
    private Long allocationId;
    private Boolean isIssued;
    private Boolean isRejected;
    private String rejectedReason;
}