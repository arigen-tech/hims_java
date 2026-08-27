package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StoreInternalIssueDetailRequest {
    private Long indentTId;
    private BigDecimal issuedQty;
    private BigDecimal availableStock;
    private BigDecimal batchStock;
    private Long itemId;
    private String batchNo;
    private Long manufacturerId;
    private LocalDate expiryDate;
    private  LocalDate manufactureDate;
    private  Long stockId;
//    private BigDecimal itemCost;

}
