package com.hims.request;

import com.hims.entity.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

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



  //  private String dosage;



}
