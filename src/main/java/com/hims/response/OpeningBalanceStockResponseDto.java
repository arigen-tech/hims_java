package com.hims.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OpeningBalanceStockResponseDto {

    private Long stockId;
    private Long itemId;
    private String itemName;
    private String itemCode;
    private Long openingQty;
    private String unitAu;
    private String batchNo;
    private LocalDate dom;
    private LocalDate doe;
    private String manufacturerName;
    private String sectionName;
    private Integer sectionId;
    private Integer classId;
    private String className;
    private String medicineSource;
    private BigDecimal mrpPerUnit;
    private Long closingQty;
}