package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_stock_ledger")
@Getter
@Setter
@NoArgsConstructor
public class StoreStockLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ledger_id")
    private Long ledgerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "stock_id")
    private  StoreItemBatchStock stockId;

    @Column(name = "txn_type", length = 30)
    private String txnType;

    @Column(name = "txn_date")
    private LocalDate txnDate;

    @Column(name = "txn_reference_id")
    private Long txnReferenceId;

    @Column(name = "qty_in")
    private Long qtyIn;

    @Column(name = "qty_out")
    private Long qtyOut;

    @Column(name = "remarks", columnDefinition = "text")
    private String remarks;

    @Column(name = "created_by", length = 150)
    private String createdBy;

    @Column(name = "created_dt")
    private LocalDateTime createdDt;
}
