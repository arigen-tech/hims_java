package com.hims.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OtSurgeryConsumableResponse {
    private Long otSurgeryConsumableId;
    private Long otSurgeryExecutionId;
    private Long itemId;
    private String itemName;
    private BigDecimal quantity;
    private BigDecimal plannedQuantity;
    private String batchNo;
    private String remarks;
    private LocalDateTime lastChgDate;
}
