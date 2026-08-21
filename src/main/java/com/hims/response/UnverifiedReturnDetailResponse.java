package com.hims.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnverifiedReturnDetailResponse {

    private Long returnTId;

    private Long returnMId;

    private Long itemId;
    private String itemName;

    private Long stockId;
    private String batchNo;

    private LocalDate expiryDate;
    private LocalDate dom;

    private String brandName;
    private String manufacturerName;

    private BigDecimal rejectedQty;
    private BigDecimal usableQty;
    private BigDecimal damagedQty;

    private String returnReason;
    private String storeVerification;
}