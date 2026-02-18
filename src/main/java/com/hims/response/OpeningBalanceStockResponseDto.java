package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OpeningBalanceStockResponseDto {
    private Long stockId;
    private Long itemId;
    private String itemName;
    private String itemCode;
    private Long OpeningQty;
    private Long closingQty;
    private String unitAu;
    private String batchNo;
    private LocalDate dom;
    private LocalDate doe;
    private String manufacturerName;
    private String medicineSource;
    private Integer sectionId;
    private String sectionName;
    private Integer classId;
    private String className;
    private BigDecimal mrpPerUnit;
}
