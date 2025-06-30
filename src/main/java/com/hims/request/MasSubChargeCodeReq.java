package com.hims.request;

import lombok.Data;

@Data
public class MasSubChargeCodeReq {
    private String subCode;
    private String subName;
    private Long mainChargeId;
}
