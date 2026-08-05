package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MasStoreItemDetails {

        private Long itemId;
        private String pvmsNo;
        private String nomenclature;
        private String status;
        private Long lastChgBy;
        private LocalDate lastChgDate;
        private String lastChgTime;
        private Long requestedDeptStocks;
        private Long currentDeptStocks;
        private BigDecimal adispQty;
        private Long hospitalId;
        private Long departmentId;

        private Long unitAU;
        private Long dispUnit;
        private Integer sectionId;
        private Integer itemTypeId;
        private Integer groupId;
        private Integer itemClassId;
        private Integer masItemCategoryid;
        private String hsnCode;
        private String masItemCategoryName;
        private String unitAuName;
        private String dispUnitName;
        private String sectionName;
        private String itemTypeName;
        private String groupName;
        private String itemClassName;
        private BigDecimal hsnGstPercent;
        private Integer reOrderLevelDispensary;
        private Integer reOrderLevelStore;
        private String dosageUnit;

}
