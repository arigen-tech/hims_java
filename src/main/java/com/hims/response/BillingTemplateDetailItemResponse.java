package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class BillingTemplateDetailItemResponse {
    private Long templateDetailsId;
    private Long itemId;
    private String itemName;
    private String unit;
    private String type;
    private BigDecimal qty;
}
