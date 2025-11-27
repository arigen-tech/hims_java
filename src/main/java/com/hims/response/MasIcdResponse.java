package com.hims.response;

import lombok.Data;

@Data
public class MasIcdResponse {
    private Long icdId;
    private String icdCode;
    private String icdName;
    private String status;
}
