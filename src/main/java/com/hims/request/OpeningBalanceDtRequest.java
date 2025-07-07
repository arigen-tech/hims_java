package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class OpeningBalanceDtRequest {
    private Long balanceId;
    private Long itemId;
    private String batchNo;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private Long unitsPerPack;
    private BigDecimal purchaseRatePerUnit;
    private BigDecimal gstPercent;
    private BigDecimal mrpPerUnit;
    private Long qty;
    private BigDecimal totalMrp;
    private Long brandId;
    private Long manufacturerId;
}
