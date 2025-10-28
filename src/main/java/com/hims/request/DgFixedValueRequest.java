package com.hims.request;

import lombok.Data;

@Data
public class DgFixedValueRequest {
    private Long fixedId;
    private String fixedValue;
}
