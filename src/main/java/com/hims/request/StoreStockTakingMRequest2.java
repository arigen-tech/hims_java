package com.hims.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StoreStockTakingMRequest2 {
    private String approvedBy;
    private LocalDateTime approvedDt;
}
