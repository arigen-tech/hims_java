package com.hims.response;

// BillingDetailResponse.java


import lombok.Data;

import java.math.BigDecimal;

@Data
public class BillingDetailResponse {
    private Long id;
    private String itemName;
    private Integer quantity;
    private BigDecimal basePrice;
    private BigDecimal tariff;
    private BigDecimal discount;
    private BigDecimal amountAfterDiscount;
    private BigDecimal taxPercent;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal Total;
    private String paymentStatus;
    private BigDecimal registrationCost;


    private Long investigationId;
    private String investigationName;


    private Long packageId;
    private String packageName;
}


