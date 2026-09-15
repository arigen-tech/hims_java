package com.hims.request;

import lombok.Data;

@Data
public class BloodInventoryRequest {
    private Long patientBloodGroupId;
    private Long componentId;
}
