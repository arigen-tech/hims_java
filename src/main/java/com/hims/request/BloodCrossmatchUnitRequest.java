package com.hims.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BloodCrossmatchUnitRequest {

    private Long inventoryId;
    private String unitNo;
    private String compatibilityResult;
    private LocalDate testDate;
    private String remarks;
}