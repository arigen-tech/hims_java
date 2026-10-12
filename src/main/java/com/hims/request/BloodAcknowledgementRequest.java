package com.hims.request;

import lombok.Data;

@Data
public class BloodAcknowledgementRequest {
    private Long allocationId;
    private Boolean accepted;
    private String remarks;
}
