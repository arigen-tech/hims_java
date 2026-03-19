package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class MasInvestigationPriceDetailsProjectionResponse {
    private Long id;
    private Long investigationId;
    private String investigationName;
    private LocalDate fromDt;
    private LocalDate toDt;
    private BigDecimal price;
    private String status;
}
