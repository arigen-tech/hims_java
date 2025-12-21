package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StoreIndentReceiveItemRequest {
    private Long indentTId;
    private Long itemId;
    private String batchNo;
    private BigDecimal qtyIssued;
    private BigDecimal qtyReceived;
    private BigDecimal qtyRejected;
    private BigDecimal previousReceivedQty;
//    private String rejectionReason;
}
