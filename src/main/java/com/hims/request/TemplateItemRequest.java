package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TemplateItemRequest {
    private Long itemId;
    private BigDecimal qty;
}
