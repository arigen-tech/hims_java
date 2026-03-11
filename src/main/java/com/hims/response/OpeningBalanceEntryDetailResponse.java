package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class OpeningBalanceEntryDetailResponse {

    private Long balanceTId;
    private Long balanceMId;
    private Long itemId;
    private String itemName;
    private String itemUnit;
    private BigDecimal itemGst;
    private String itemCode;
    private String batchNo;
    private LocalDate manufactureDate;
    private LocalDate expiryDate;
    private Long qty;
    private Long unitsPerPack;
    private BigDecimal purchaseRatePerUnit;
    private BigDecimal gstPercent;
    private BigDecimal mrpPerUnit;
    private String hsnCode;
    private BigDecimal baseRatePerUnit;
    private BigDecimal gstAmountPerUnit;
    private BigDecimal totalPurchaseCost;
    private BigDecimal totalMrpValue;
    private Long brandId;
    private Long manufacturerId;
    private String brandName;
    private String manufacturerName;
}
