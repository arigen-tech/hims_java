package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class StoreStockTakingMRequest {
    private Long id;
    private String reasonForTraking;
    private  String status;
    List<StoreStockTakingTRequest> stockEntries;
    private List<Long> deletedT;
}
