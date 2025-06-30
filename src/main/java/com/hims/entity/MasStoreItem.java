package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name="mas_store_item")
public class MasStoreItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "pvms_no", length = 25)
    private String pvmsNo;

    @Column(name = "nomenclature", length = 250)
    private String nomenclature;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by")
    private Long lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDate lastChgDate;

    @Column(name = "last_chg_time", length = 10)
    private String lastChgTime;

    @Column(name = "a_disp_qty", precision = 19, scale = 2)
    private BigDecimal aDispQty;

    @Column(name = "hospital_id")
    private Integer hospitalId;

//    @Column(name = "department_id")
//    private Integer departmentId;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "dispensing_unit")

    private MasStoreUnit dispUnit;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "unit_au")

    private MasStoreUnit unitAU;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "section_id")

    private MasStoreSection sectionId;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "item_type_id")

    private MasItemType itemTypeId;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "group_id")

    private MasStoreGroup groupId;

    @ManyToOne(fetch =  FetchType.LAZY,optional = false)
    @JoinColumn(name = "item_class_id")
    @org.hibernate.annotations.NotFound(action = NotFoundAction.IGNORE)
    private MasItemClass itemClassId;

    @ManyToOne(fetch =  FetchType.EAGER)
    @JoinColumn(name = "hsn_code")
    private MasHSN hsnCode;

    @Column(name = "re_order_level_dispensary")
    private Integer reOrderLevelDispensary;

    @Column(name = "re_order_level_store")
    private Integer reOrderLevelStore;




}