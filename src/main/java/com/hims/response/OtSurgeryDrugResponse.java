package com.hims.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OtSurgeryDrugResponse {
    private Long otSurgeryDrugId;
    private Long otSurgeryExecutionId;
    private Long itemId;
    private String itemName;
    private BigDecimal quantity;
    private String batchNo;
    private String remarks;
    private LocalDateTime lastChgDate;
}
