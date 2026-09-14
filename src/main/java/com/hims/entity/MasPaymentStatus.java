package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Placeholder master table for payment/refund status codes (PENDING, PAID,
 * FAILED, REFUND_PENDING, REFUNDED). If your HMIS already has an equivalent
 * status master table (it very likely does, given the pattern of MasHospital,
 * MasServiceCategory, MasDiscount etc. already in your codebase), DELETE this
 * file and repoint PaymentStatusResolver at your existing table/entity instead.
 */
@Getter
@Setter
@Entity
@Table(name = "mas_payment_status")
public class MasPaymentStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_status_id", nullable = false)
    private Long id;

    @Column(name = "payment_status_code")
    private String statusCode;

    @Column(name = "payment_status_name")
    private String statusName;
}