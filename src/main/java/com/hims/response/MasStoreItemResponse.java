package com.hims.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter

public class MasStoreItemResponse {
    private Long itemId;
    private String pvmsNo;
    private String nomenclature;
    private String status;
    private Long lastChgBy;
    private LocalDate lastChgDate;
    private String lastChgTime;
    private Long storestocks;
    private Long dispstocks;
    private Long wardstocks;
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
    private BigDecimal reOrderLevelDispensary;
    private BigDecimal reOrderLevelStore;
    private String  isGeneric;
    private String dangerousDrug;
    private List<MasFacilityCodeResponse> facilityCode;
    private String drugSchedule;
    private String highValueDrug;
    private String availableInOpd;
    private String availableInIpd;
    private String availableInEmergency;
    private String availableInOt;
    @Data

    public static class MasFacilityCodeResponse{
         Long facilityId;
         String facilityCode;


    }





}
