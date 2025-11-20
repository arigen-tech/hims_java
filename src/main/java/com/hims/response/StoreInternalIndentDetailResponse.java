package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StoreInternalIndentDetailResponse {
    private Long indentTId;
    private Long itemId;
    private String itemName;
    private String pvmsNo;

    private BigDecimal requestedQty;
    private BigDecimal approvedQty;
    private BigDecimal issuedQty;
    private BigDecimal receivedQty;
    private BigDecimal availableStock;
    private BigDecimal itemCost;
    private BigDecimal totalCost;
    private String issueStatus;
}
