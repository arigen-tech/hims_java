package com.hims.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "mas_store_item")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MasStoreItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
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

    // Mapping 1: Dispensing Unit
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispensing_unit")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private MasStoreUnit dispUnit;

    // Mapping 2: Unit AU
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_au")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private MasStoreUnit unitAU;

    // Mapping 3: Section
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private MasStoreSection sectionId;

    // Mapping 4: Item Type
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_type_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private MasItemType itemTypeId;

    // Mapping 5: Group
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private MasStoreGroup groupId;

    // Mapping 6: Item Class
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_class_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private MasItemClass itemClassId;

    // Mapping 7: HSN Code
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hsn_code")
    @NotFound(action = NotFoundAction.IGNORE)
    @JsonIgnoreProperties(ignoreUnknown = true)
    private MasHSN hsnCode;

    @Column(name = "re_order_level_dispensary")
    private Integer reOrderLevelDispensary;

    @Column(name = "re_order_level_store")
    private Integer reOrderLevelStore;
}
