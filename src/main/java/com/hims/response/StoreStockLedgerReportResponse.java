package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoreStockLedgerReportResponse {

    private Long ledgerId;
    private LocalDateTime createdDate;
    private String txnType;
    private String referenceNum;
    private String txnSource;
    private BigDecimal qtyBefore;
    private BigDecimal qtyIn;
    private  BigDecimal qtyOut;
    private BigDecimal qtyReturn;
    private BigDecimal qtyAfter;
    private String remarks;

}
