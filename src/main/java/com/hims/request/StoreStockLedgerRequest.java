package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreStockLedgerRequest {
    private Long stockId;
    private  String txnType;
    private Long txnReferenceId;
    private BigDecimal qtyIn;
    private BigDecimal qtyOut;
    private BigDecimal qtyReject;
    private String remarks;
    private BigDecimal qtyBefore;
    private BigDecimal qtyAfter;
    private String txnSource;
    private String referenceNo;
    private Long departmentId;
    private Long hospitalId;
    private String createdBy;
}
