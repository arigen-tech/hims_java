package com.hims.request;

import lombok.Data;

@Data
public class MasIntakeTypeRequest {
    private String intakeTypeName;
    private String isLiquid;
}
