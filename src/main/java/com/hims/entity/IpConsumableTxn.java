package com.hims.entity;

import jakarta.mail.FetchProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ip_consumable_txn", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IpConsumableTxn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consumable_txn_id")
    private Long consumableTxnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MasStoreItem itemId;

    @Column(name = "item_name", length = 300)
    private String itemName;

    @Column(name = "uom", length = 50)
    private String uom;

    @Column(name = "quantity", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantity;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "usage_datetime", nullable = false)
    private LocalDateTime usageDatetime;

    @Column(name = "used_by", length = 100)
    private String usedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_txn_id")
    private IpProcedureTxn procedureTxnId;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "is_billed")
    private Boolean isBilled = false;

    @Column(name = "bill_item_id")
    private Long billItemId;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}