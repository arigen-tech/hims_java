package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter

public class MasStoreItemResponseWithStock {
    private Long itemId;
    private String pvmsNo;
    private String nomenclature;
    private Long storestocks;
    private BigDecimal adispQty;
    private Long unitAU;
    private Long dispUnit;
    private Integer sectionId;
    private String unitAuName;
    private String dispUnitName;

}
