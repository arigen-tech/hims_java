package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name="mas_store_item")
public class MasStoreItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", nullable = false)
    private Integer itemId;

    @Column(name = "pvms_no", length = 25)
    private String pvmsNo;

    @Column(name = "nomenclature", length = 250)
    private String nomenclature;

//    @Column(name = "cost_price", length = 10)
//    private String costPrice;
//
//    @Column(name = "dangerous_drug", length = 1)
//    private String dangerousDrug;
//
//    @Column(name = "pac", length = 1)
//    private String pac;
//
//    @Column(name = "controlled_drug", length = 1)
//    private String controlledDrug;
//
//    @Column(name = "high_value_drug", length = 1)
//    private String highValueDrug;
//
//    @Column(name = "sales_tax")
//    private Double salesTax;
//
//    @Column(name = "rate_contract_item", length = 1)
//    private String rateContractItem;
//
//    @Column(name = "rol", length = 25)
//    private String rol;
//
//    @Column(name = "max_stock")
//    private Double maxStock;
//
//    @Column(name = "min_stock")
//    private Double minStock;
//
//    @Column(name = "self_life", length = 10)
//    private String selfLife;
//
//    @Column(name = "lead_time", length = 10)
//    private String leadTime;
//
//    @Column(name = "location", length = 10)
//    private String location;
//
//    @Column(name = "specification", length = 50)
//    private String specification;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by")
    private Long lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDate lastChgDate;

    @Column(name = "last_chg_time", length = 10)
    private String lastChgTime;

//    @Column(name = "old_niv_no", length = 10)
//    private String oldNivNo;
//
//    @Column(name = "non_pac", length = 1)
//    private String nonPac;
//
//    @Column(name = "source_of_supply", length = 1)
//    private String sourceOfSupply;
//
//    @Column(name = "slow_moving_days")
//    private Integer slowMovingDays;
//
//    @Column(name = "fast_moving_days")
//    private Integer fastMovingDays;
//
//    @Column(name = "non_moving_days")
//    private Integer nonMovingDays;
//
//    @Column(name = "strength", length = 30)
//    private String strength;
//
//    @Column(name = "expiry", length = 1)
//    private String expiry;
//
//    @Column(name = "allergy", length = 50)
//    private String allergy;
//
//    @Column(name = "sophisticated_item", length = 1)
//    private String sophisticatedItem;
//
//    @Column(name = "ppp_item", length = 1)
//    private String pppItem;
//
//    @Column(name = "common_name", length = 250)
//    private String commonName;
//
//    @Column(name = "high_risk_medicine", length = 1)
//    private String highRiskMedicine;
//
//    @Column(name = "abc", length = 30)
//    private String abc;
//
//    @Column(name = "ved", length = 30)
//    private String ved;
//
//    @Column(name = "group_123", length = 30)
//    private String group123;
//
//    @Column(name = "remarks", length = 200)
//    private String remarks;
//
//    @Column(name = "branded_generic", length = 10)
//    private String brandedGeneric;
//
//    @Column(name = "temperature", length = 10)
//    private String temperature;
//
//    @Column(name = "salt", length = 250)
//    private String salt;



    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "dispensing_unit")
    private MasStoreUnit dispUnit;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "unit_au")
    private MasStoreUnit unitAU;

    @Column(name = "a_disp_qty", precision = 19, scale = 2)
    private BigDecimal aDispQty;

//    @Column(name = "item_type", length = 1)
//    private String itemType;

    @Column(name = "hospital_id")
    private Integer hospitalId;

//    @Column(name = "department_id")
//    private Integer departmentId;
//
//    @Column(name = "manufacturer_id")
//    private Integer manufacturerId;
//
//    @Column(name = "brand_id")
//    private Integer brandId;

//    @Column(name = "supplier_id")
//    private Integer supplierId;
//
//    @Column(name = "item_generic_id")
//    private Integer itemGenericId;
//
//    @Column(name = "item_conversion_id")
//    private Integer itemConversionId;



    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "section_id")
    private MasStoreSection sectionId;

//    @Column(name = "item_category_id")
//    private Integer itemCategoryId;



    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "item_type_id")
    private MasItemType itemTypeId;


    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private MasStoreGroup groupId;
//
//    @Column(name = "company_id")
//    private Integer companyId;

//    @Column(name = "intermediate_unit_id")
//    private Integer intermediateUnitId;


      @ManyToOne(fetch =  FetchType.LAZY)
      @JoinColumn(name = "item_class_id")
      private MasItemClass itemClassId;


//    @Column(name = "insulin_injection", length = 1)
//    private String insulinInjection;

//    @Column(name = "item_classification_id")
//    private Integer itemClassificationId;

//    @Column(name = "issue_from", length = 1)
//    private String issueFrom;
//
//    @Column(name = "prescribed_from", length = 1)
//    private String prescribedFrom;
    @Column(name = "re_order_level_dispensary")
    private Integer reOrderLevelDispensary;

    @Column(name = "re_order_level_store")
    private Integer reOrderLevelStore;


}
