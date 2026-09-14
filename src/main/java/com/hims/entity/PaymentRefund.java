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
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "payment_refund")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PaymentRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refund_id", nullable = false)
    private Long refundId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_id", nullable = false)
    private PaymentDetailsV2 payment;

    // Point at your mas_payment_mode (or equivalent) master table id.
    @NotNull
    @Column(name = "refund_mode_id", nullable = false)
    private Long refundModeId;

    @NotNull
    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    // Resolved from your payment-status master table (REFUND_PENDING / REFUNDED).
    @NotNull
    @Column(name = "refund_status_id", nullable = false)
    private Long refundStatusId;

    @NotNull
    @Size(max = 100)
    @Column(name = "refund_reference_no", nullable = false, length = 100, unique = true)
    private String refundReferenceNo;

    // Point at your payment-gateway master table id (e.g. mas_payment_gateway -> RAZORPAY row).

    @Column(name = "payment_gateway_id", nullable = false)
    private Long paymentGatewayId;


    @Size(max = 100)
    @Column(name = "gateway_refund_id", nullable = false, length = 100, unique = true)
    private String gatewayRefundId;

    @NotNull
    @Size(max = 500)
    @Column(name = "refund_reason", nullable = false, length = 500)
    private String refundReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refund_reason_id")
    private MasAppointmentChangeReason appointmentChangeReason;

    @NotNull
    @Column(name = "refund_requested_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime refundRequestedAt;

    @Column(name = "refund_processed_at")
    private LocalDateTime refundProcessedAt;

    @NotNull
    @Size(max = 50)
    @Column(name = "created_by", nullable = false, length = 50)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Size(max = 50)
    @Column(name = "updated_by", nullable = false, length = 50)
    private String updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

//ARN/RRN or UTC no
    @Size(max = 100)
    @Column(name = "gateway_reference_no", length = 100)
    private String gatewayReferenceNo;
//ARN/RRN,UTC
    @Size(max = 10)
    @Column(name = "gateway_reference_type", length = 10)
    private String gatewayReferenceType;


}