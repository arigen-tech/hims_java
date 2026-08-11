package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStoreItemBatchStockRequest {

    private Long stockId;
    private Long departmentId;
    private Long itemId;
    private Long brandId;
    private Long manufacturerId;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private BigDecimal opdIssueQty;
    private String lastChgBy;

}
