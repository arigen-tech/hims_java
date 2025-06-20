package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "payment_details")
public class PaymentDetail {
    @Id
    @Column(name = "payment_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "billing_hd_id", nullable = false)
    private BillingHeader billingHd;

    @Size(max = 50)
    @NotNull
    @Column(name = "payment_mode", nullable = false, length = 50)
    private String paymentMode;

    @Size(max = 20)
    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    @Size(max = 100)
    @Column(name = "payment_reference_no", length = 100)
    private String paymentReferenceNo;

    @Column(name = "payment_date")
    private Instant paymentDate;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Size(max = 50)
    @NotNull
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

}
