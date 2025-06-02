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
@Table(name = "billing_payments")
public class BillingPayment {
    @Id
    @Column(name = "payment_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "billing_hd_id", nullable = false)
    private BillingHeader billingHd;

    @Column(name = "payment_date")
    private Instant paymentDate;

    @NotNull
    @Column(name = "payment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paymentAmount;

    @Size(max = 20)
    @NotNull
    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod;

    @Size(max = 50)
    @Column(name = "payment_reference_number", length = 50)
    private String paymentReferenceNumber;

    @Size(max = 1)
    @Column(name = "payment_status", length = 1)
    private String paymentStatus;

    @Size(max = 50)
    @NotNull
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

}
