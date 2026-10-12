package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class ConsumableEntryRequest {
    private Long itemId;
    private Long InpatientId;
    private BigDecimal requestQty;
    private Long batchStockId;
    private Long procedureId;
}
