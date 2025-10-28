package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StoreStockTakingTRequest {
    private Long id;
    private String batchNo;
    private LocalDate doe;
    private BigDecimal computedStock;
    private BigDecimal storeStockService;
    private String remarks;
    private BigDecimal stockSurplus;
    private BigDecimal stockDeficient;
    private Long stockId;
    private Long itemId;
    private Long trakingMId;


}
