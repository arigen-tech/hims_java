package com.hims.request;

import com.hims.entity.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Data
public class MasStoreItemRequest {
    private String pvmsNo;
    private String nomenclature;
    private Integer groupId;
    private Integer itemTypeId;
    private Integer sectionId;
    private Integer itemClassId;
    private Integer masItemCategoryId;
    private Long unitAU;
    private Long dispUnit;
    private BigDecimal adispQty;
    private BigDecimal  reOrderLevelDispensary;
    private BigDecimal  reOrderLevelStore;
    private BigDecimal  reOrderLevelWard;
    private String hsnCode;
    private String  isGeneric;
    private String dangerousDrug;
    private List<Long> facility;
    private String drugSchedule;
    //  private String dosage;
    private String highValueDrug;
    private String availableInOpd;
    private String availableInIpd;
    private String availableInEmergency;
    private String availableInOt;
    private String dosageUnit;

}
