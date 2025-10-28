package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;

@Data

public class StoreItemBatchStockRequest {
    private BigDecimal mrpValue;
}
