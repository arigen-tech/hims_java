package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
@Table(name = "store_stock_taking_t", schema = "public")
public class StoreStockTakingT {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "taking_t_id")
    private Long takingTId;

    @Column(name = "batch_no", length = 120)
    private String batchNo;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "computed_stock")
    private BigDecimal computedStock;

    @Column(name = "store_stock_service")
    private BigDecimal storeStockService;

    @Column(name = "remarks", length = 200)
    private String remarks;

    @Column(name = "stock_surplus")
    private BigDecimal stockSurplus;

    @Column(name = "stock_deficient")
    private BigDecimal stockDeficient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_id")
    private StoreItemBatchStock stockId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private MasStoreItem itemId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "taking_m_id")
    private StoreStockTakingM takingMId;

    @Column(name = "is_approved")
    private Boolean isApproved = false;
}
