package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StoreInternalIndentDetailRequest {
    private Long indentTId;       // null for new
    private Long itemId;
    private BigDecimal requestedQty;
//    private BigDecimal approvedQty;
//    private BigDecimal issuedQty;
//    private BigDecimal receivedQty;
    private BigDecimal availableStock;
//    private BigDecimal itemCost;
//    private BigDecimal totalCost;
    private String issueStatus;
    private String reason;
}
