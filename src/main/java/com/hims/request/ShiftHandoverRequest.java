package com.hims.request;

import lombok.Data;

@Data
public class ShiftHandoverRequest {
    private String notes;
    private Long inpatientId;
}
