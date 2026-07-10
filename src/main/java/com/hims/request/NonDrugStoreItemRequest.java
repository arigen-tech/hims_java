package com.hims.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class NonDrugStoreItemRequest {
    private String pvmsNo;
    private String nomenclature;
    private Integer groupId;
    private Integer itemTypeId;
    private Integer sectionId;
    private Integer itemClassId;
    private Integer masItemCategoryId;
    private Long unitAU;

}
