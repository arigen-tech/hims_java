package com.hims.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_item_batch_stock")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreItemBatchStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stock_id")
    private Long stockId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_id", nullable=false)
    private MasHospital hospitalId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id", nullable=false)
    private MasDepartment departmentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private MasStoreItem itemId;



    @Column(name = "batch_no")
    private String batchNo;

    @Column(name = "manufacture_date")
    private LocalDate manufactureDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "opening_balance_qty")
    private Long openingBalanceQty;

    @Column(name = "po_received_qty")
    private Long poReceivedQty;

    @Column(name = "indent_received_qty")
    private Long indentReceivedQty;

    @Column(name = "indent_issue_qty")
    private Long indentIssueQty;

    @Column(name = "opd_issue_qty")
    private Long opdIssueQty;

    @Column(name = "stock_surplus")
    private Long stockSurplus;

    @Column(name = "stock_deficient")
    private Long stockDeficient;

    @Column(name = "closing_stock")
    private Long closingStock;

    @Column(name = "qty")
    private Long qty;

    @Column(name = "units_per_pack")
    private Long unitsPerPack;

    @Column(name = "purchase_rate_per_unit", precision = 12, scale = 2)
    private BigDecimal purchaseRatePerUnit;

    @Column(name = "gst_percent", precision = 12, scale = 2)
    private BigDecimal gstPercent;

    @Column(name = "mrp_per_unit", precision = 12, scale = 2)
    private BigDecimal mrpPerUnit;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hsn_code")
    private MasHSN hsnCode;

    @Column(name = "gst_amount_per_unit", precision = 12, scale = 2, insertable = false, updatable = false)
    private BigDecimal gstAmountPerUnit;

    @Column(name = "total_purchase_cost", precision = 12, scale = 2, insertable = false, updatable = false)
    private BigDecimal totalPurchaseCost;

    @Column(name = "total_mrp_value", precision = 12, scale = 2, insertable = false, updatable = false)
    private BigDecimal totalMrpValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private MasBrand brandId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manufacturer_id")
    private MasManufacturer manufacturerId;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

    @Column(name = "last_chg_by")
    private String lastChgBy;
}
