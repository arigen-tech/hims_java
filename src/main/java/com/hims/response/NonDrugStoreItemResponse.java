package com.hims.response;

import lombok.Data;

@Data
public class NonDrugStoreItemResponse {
    private Long itemId;
    private String pvmsNo;
    private String nomenclature;
    private Integer groupId;
    private String groupName;
    private Integer itemTypeId;
    private String itemTypeName;
    private Integer sectionId;
    private String sectionName;
    private Integer itemClassId;
    private String itemClassName;
    private Integer masItemCategoryId;
    private String masItemCategoryName;
    private Long unitAU;
    private String unitAuName;
    private String status;
    private String hsn;
}
