package com.hims.request;

import lombok.Data;

@Data
public class DgNormalValueRequest {
    private Long normalId;
    private String sex;
    private Long fromAge;
    private Long toAge;
    private String minNormalValue;
    private String maxNormalValue;
    private String normalValue;
    private Long mainChargeCodeId;
}
