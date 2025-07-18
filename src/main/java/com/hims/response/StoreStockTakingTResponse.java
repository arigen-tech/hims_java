package com.hims.response;

import com.hims.entity.MasStoreItem;
import com.hims.entity.StoreItemBatchStock;
import com.hims.entity.StoreStockTakingM;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StoreStockTakingTResponse {

    private Long takingTId;
    private String batchNo;
    private LocalDate expiryDate;
    private BigDecimal computedStock;
    private BigDecimal storeStockService;
    private String remarks;
    private BigDecimal stockSurplus;
    private BigDecimal stockDeficient;
    private Long stockId;
    private Long itemId;
    private  String itemName;
    private  String itemCode;
    private Long takingMId;

}
