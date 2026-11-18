package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BloodInventoryStockSummaryResponse {

    private String bloodGroupName;
    private String componentCode;
    private Long available;
    private Long reserved;
    private Long allocated;
    private Long expired;
}