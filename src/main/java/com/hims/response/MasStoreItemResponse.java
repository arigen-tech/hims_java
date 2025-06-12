package com.hims.response;

import com.hims.entity.MasStoreUnit;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class MasStoreItemResponse {

    private Integer itemId;

    private String pvmsNo;

    private String nomenclature;


//    private String costPrice;
//

//    private String dangerousDrug;
//

//    private String pac;
//

//    private String controlledDrug;
//

//    private String highValueDrug;
//

//    private Double salesTax;
//

//    private String rateContractItem;
//

//    private String rol;
//

//    private Double maxStock;
//

//    private Double minStock;
//

//    private String selfLife;
//

//    private String leadTime;
//

//    private String location;
//

//    private String specification;


    private String status;


    private Long lastChgBy;


    private LocalDate lastChgDate;


    private String lastChgTime;


//    private String oldNivNo;
//

//    private String nonPac;
//

//    private String sourceOfSupply;
//

//    private Integer slowMovingDays;
//

//    private Integer fastMovingDays;
//

//    private Integer nonMovingDays;
//

//    private String strength;
//

//    private String expiry;
//

//    private String allergy;
//

//    private String sophisticatedItem;
//

//    private String pppItem;
//

//    private String commonName;
//

//    private String highRiskMedicine;
//

//    private String abc;
//

//    private String ved;
//

//    private String group123;
//

//    private String remarks;
//

//    private String brandedGeneric;
//

//    private String temperature;
//

//    private String salt;



    private Long unitAU;
    private Long dispUnit;


    private BigDecimal aDispQty;


//    private String itemType;


    private Integer hospitalId;


//    private Integer departmentId;
//

//    private Integer manufacturerId;
//

//    private Integer brandId;


//    private Integer supplierId;
//

//    private Integer itemGenericId;
//

//    private Integer itemConversionId;




    private Integer sectionId;


//    private Integer itemCategoryId;




    private Integer itemTypeId;


    private Integer groupId;
//

//    private Integer companyId;


//    private Integer intermediateUnitId;



    private Integer itemClassId;



//    private String insulinInjection;


//    private Integer itemClassificationId;

//    private String issueFrom;
//

//    private String prescribedFrom;

    private Integer reOrderLevelDispensary;


    private Integer reOrderLevelStore;



}
