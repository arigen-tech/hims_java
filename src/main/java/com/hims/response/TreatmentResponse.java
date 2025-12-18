package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public  class TreatmentResponse {
    private Long treatmentTemp;
    private String dosage;
    private Long noOfDays;
    private Long total;
    private String instruction;
    private Long frequencyId;
    private Long itemId;
    private String itemName;
    private String dispU;
    private Long stocks;
    private String dispUnit;
    private Integer itemClassId;
    private BigDecimal adispQty;
}

