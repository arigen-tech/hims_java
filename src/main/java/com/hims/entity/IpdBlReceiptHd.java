package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "ipd_bl_receipt_hd",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ipd_receipt_no", columnNames = "receipt_no")
        }
)
public class IpdBlReceiptHd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "receipt_id")
    private Long receiptId;

    @Column(name = "receipt_no", nullable = false, length = 50)
    private String receiptNo;

    @Column(name = "receipt_date", nullable = false)
    private LocalDateTime receiptDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false, foreignKey = @ForeignKey(name = "fk_receipt_hd_inpatient"))
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", foreignKey = @ForeignKey(name = "fk_receipt_hd_bill"))
    private IpdBillingHeader bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_type_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_receipt_hd_receipt_type"))
    private MasReceiptType receiptType;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "receipt_status", length = 1)
    private String receiptStatus;

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "cancelled_by", length = 100)
    private String cancelledBy;

    @Column(name = "cancelled_date")
    private LocalDateTime cancelledDate;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_chg_by", length = 100)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;


}