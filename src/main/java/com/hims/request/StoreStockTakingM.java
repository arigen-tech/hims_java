package com.hims.request;

import lombok.Data;

import java.util.List;

@Data
public class StoreStockTakingM {
    private Long id;
    private String reasonForTraking;
    List<StoreStockTakingT> stockEntries;
}
