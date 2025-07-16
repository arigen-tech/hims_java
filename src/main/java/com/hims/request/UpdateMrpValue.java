package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateMrpValue {
    private Long stockId;
    private BigDecimal mrpValue;
}
