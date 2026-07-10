package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class IpdServiceCategoryResponse {

    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private Integer displayOrder;
    private String isSubcategoryRequired;
    private String gstApplicable;
    private BigDecimal gstPercentage;
    private String status;
    private LocalDateTime lastUpdate;
}