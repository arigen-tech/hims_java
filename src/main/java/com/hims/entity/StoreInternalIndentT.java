package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "store_internal_indent_t")
@Data
public class StoreInternalIndentT {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "indent_t_id")
    private Long indentTId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indent_m_id", nullable = false)
    private StoreInternalIndentM indentM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private MasStoreItem itemId;

    @Column(name = "requested_qty", precision = 14, scale = 2)
    private BigDecimal requestedQty;

    @Column(name = "approved_qty", precision = 14, scale = 2)
    private BigDecimal approvedQty;

    @Column(name = "issued_qty", precision = 14, scale = 2)
    private BigDecimal issuedQty;

    @Column(name = "received_qty", precision = 14, scale = 2)
    private BigDecimal receivedQty;

    @Column(name = "available_stock", precision = 14, scale = 2)
    private BigDecimal availableStock;

    @Column(name = "item_cost", precision = 14, scale = 2)
    private BigDecimal itemCost;

//    @Column(name = "total_cost", precision = 14, scale = 2)
//    private BigDecimal totalCost;

    @Column(name = "total_cost", precision = 14, scale = 2, insertable = false, updatable = false)
    private BigDecimal totalCost;


    @Column(name = "issue_status", length = 2)
    private String issueStatus;
}
