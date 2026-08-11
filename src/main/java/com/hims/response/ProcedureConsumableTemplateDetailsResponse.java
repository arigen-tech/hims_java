package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
public class ProcedureConsumableTemplateDetailsResponse {
    private Long templateDetailId;
    private Long itemId;
    private BigDecimal qty;
    private String itemName;
}
