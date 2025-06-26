package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
@Getter
@Setter
public class MasStoreItemResponse2 {
    private Long id;
    private String code;
    private String name;
    private String unit;
    private String dispUnit;
    private BigDecimal hsnGstPercentage;
    private String HsnCode;



}
