package com.hims.request;

import lombok.Data;

@Data
public class DgFixedValueRequest {
    private String fixedValue;
    private Long subChargeCodeId;
}
