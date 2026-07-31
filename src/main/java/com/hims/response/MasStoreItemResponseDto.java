package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class MasStoreItemResponseDto {
    private Long id;
    private String code;
    private String name;
    private String unit;
    private String dispUnit;
    private BigDecimal hsnGstPercentage;
    private String HsnCode;
    private String itemClassName;
    private BigDecimal aDispQty;


}
