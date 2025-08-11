package com.hims.response;

import lombok.Data;

@Data
public class OpeningBalanceStockResponse {
    private Long stockId;
    private Long itemId;
    private String itemName;
    private String itemCode;
    private Long OpeningQty;
    private Long closingQty;
    private String unitAu;
    private Integer sectionId;
    private String sectionName;
    private Integer classId;
    private String className;
}
