package com.hims.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BloodInventoryAvailableStockResponse {

    private Long inventoryId;
    private String unitNo;
    private String bagNumber;
    private String componentCode;
    private String bloodGroupCode;
    private LocalDate expiryDate;
    private String statusCode;
    private Integer volumeMl;
    private Boolean isExpired;
}
