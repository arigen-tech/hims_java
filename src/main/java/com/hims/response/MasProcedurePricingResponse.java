package com.hims.response;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MasProcedurePricingResponse {

    private Long procedurePricingId;
    private Long procedureId;
    private String procedureName;
    private BigDecimal basePrice;
    private String discountAllowed;
    private BigDecimal discount;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private String status;
    private Long billingTypeId;
    private String billingTypeName;
}