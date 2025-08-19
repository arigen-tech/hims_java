package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Entity
@Table(name = "store_issue_t")
@Data
public class StoreIssueT {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "store_issue_t_id")
    private Long storeIssueTId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_issue_m_id")
    private StoreIssueM storeIssueMId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private MasStoreItem itemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_t_id")
    private StoreInternalIndentT indentTId;

    @Column(name = "prescription_dt_id")
    private Long prescriptionDtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id")
    private StoreItemBatchStock stockId;

    @Column(name = "issued_qty", precision = 14, scale = 2)
    private BigDecimal issuedQty;

    @Column(name = "unit_price", precision = 14, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount", precision = 5, scale = 2)
    private BigDecimal discount;

    @Column(name = "gst_rate", precision = 5, scale = 2)
    private BigDecimal gstRate;

    @Column(name = "line_cost", precision = 14, scale = 2)
    private BigDecimal lineCost;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "status", length = 2)
    private String status;
}
