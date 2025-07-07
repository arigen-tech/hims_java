package com.hims.entity;


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
    @JoinColumn(name = "hospital_id")
    private MasHospital hospitalId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
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

    @Column(name = "opening_stock")
    private Long openingStock;

    @Column(name = "received_qty")
    private Long receivedQty;

    @Column(name = "issued_qty")
    private Long issuedQty;

    @Column(name = "balance_qty")
    private Long balanceQty;

    @Column(name = "stock_inward_qty")
    private Long stockInwardQty;

    @Column(name = "stock_outward_qty")
    private Long stockOutwardQty;

    @Column(name = "stock_deficit_qty")
    private Long stockDeficitQty;

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

    @Column(name = "gst_amount_per_unit", precision = 12, scale = 2)
    private BigDecimal gstAmountPerUnit;

    @Column(name = "total_purchase_cost", precision = 12, scale = 2)
    private BigDecimal totalPurchaseCost;

    @Column(name = "total_mrp_value", precision = 12, scale = 2)
    private BigDecimal totalMrpValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private MasBrand brandId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "manufacturer_id")
    private MasManufacturer manufacturerId;

    @Column(name = "last_updated_dt")
    private LocalDateTime lastUpdatedDt;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
