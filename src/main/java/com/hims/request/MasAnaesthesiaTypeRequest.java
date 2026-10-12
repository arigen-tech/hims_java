package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MasAnaesthesiaTypeRequest {

    private String anaesthesiaTypeCode;
    private String anaesthesiaTypeName;
    private BigDecimal price;
}
