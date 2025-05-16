package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasInvestigationPriceDetailsRequest {
    private Long investigationId;
    private LocalDate fromDt;
    private LocalDate toDt;
    private BigDecimal price;
    private String status;
}
