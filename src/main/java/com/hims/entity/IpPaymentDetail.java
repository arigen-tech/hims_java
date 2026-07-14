package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ip_payment_details", schema = "public")
public class IpPaymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id")
    private IpdBillingHeader bill;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_mode_id")
    private MasPaymentMode paymentMode;

    @Column(name = "amount",  precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_status_id")
    private MasIpdPaymentStatus paymentStatus;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "last_chg_by", length = 300)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private LocalDateTime lastChgDate;

    @Column(name = "status", length = 1)
    private String status;


}