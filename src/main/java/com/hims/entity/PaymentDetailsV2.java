package com.hims.entity;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "payment_details_v2")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PaymentDetailsV2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", nullable = false)
    private Long paymentId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "billing_hd_id", referencedColumnName = "bill_hd_id", nullable = false)
    private BillingHeader billingHeader;

    // Actual payment mode as confirmed by Razorpay (UPI/Card/Netbanking/etc).
    // Point this at your mas_payment_mode (or equivalent) master table id.
    @Column(name = "payment_mode_id")
    private Long paymentModeId;


    @Column(name = "payment_via", length = 150)
    private String paymentVia;

    // HMIS payment status (PENDING / PAID / FAILED / REFUND_PENDING / REFUNDED).
    // Resolved from your payment-status master table - never hardcode the id.
    @NotNull
    @Column(name = "payment_status_id", nullable = false)
    private Long paymentStatusId;

    @NotNull
    @Size(max = 100)
    @Column(name = "payment_reference_no", nullable = false, length = 100, unique = true)
    private String paymentReferenceNo;

    @Column(name = "payment_date")
    private LocalDateTime paymentDate;

    @NotNull
    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @NotNull
    @Size(max = 10)
    @Column(name = "currency", nullable = false, length = 10)
    private String currency = "INR";

    @Size(max = 100)
    @Column(name = "receipt_no", length = 100)
    private String receiptNo;

    @NotNull
    @Size(max = 30)
    @Column(name = "payment_gateway", nullable = false, length = 30)
    private String paymentGateway = "RAZORPAY";


    @Size(max = 100)
    @Column(name = "gateway_order_id", length = 100)
    private String gatewayOrderId;

    @Size(max = 100)
    @Column(name = "gateway_payment_id", length = 100)
    private String gatewayPaymentId;

    @Size(max = 50)
    @Column(name = "gateway_payment_status", length = 50)
    private String gatewayPaymentStatus;

    @NotNull
    @Size(max = 50)
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentWebhookEvent> webhookEvents = new ArrayList<>();

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PaymentRefund> refunds = new ArrayList<>();




}
