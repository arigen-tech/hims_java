package com.hims.request;

import com.hims.entity.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class MasStoreItemRequest {
    private String pvmsNo;
    private String nomenclature;
    private Long dispUnit;
    private String status;
    private Long unitAU;
    private Integer sectionId;
    private BigDecimal aDispQty;
    private Integer itemTypeId;
    private Integer groupId;
    private Integer itemClassId;
    private Integer reOrderLevelDispensary;
    private Integer reOrderLevelStore;


}
