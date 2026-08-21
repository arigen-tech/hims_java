package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MasSurgeryPricingResponse {

    private Long surgeryPricingId;
    private Long surgeryId;
    private String surgeryName;
    private Long billingTypeId;
    private String billingTypeName;
    private BigDecimal amount;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String remarks;
    private String status;
}