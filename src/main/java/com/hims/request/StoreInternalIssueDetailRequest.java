package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StoreInternalIssueDetailRequest {
    private Long indentTId;
    private BigDecimal issuedQty;
    private BigDecimal availablestock;
//    private BigDecimal itemCost;

}
