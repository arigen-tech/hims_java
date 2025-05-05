package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasInvestigationPriceDetailsResponse {
    private Long id;
    private Long investigationId;
    private LocalDate fromDt;
    private LocalDate toDt;
    private LocalTime lastChgDt;
    private String status;
    private BigDecimal price;
}