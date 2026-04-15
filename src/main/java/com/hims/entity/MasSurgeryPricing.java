package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "mas_surgery_pricing", schema = "public")
@Data
public class MasSurgeryPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "surgery_pricing_id")
    private Long surgeryPricingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surgery_id")
    private MasSurgery surgery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_type_id")
    private MasIpdBillingType billingType;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "discount_allowed", length = 1)
    private String discountAllowed;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_updated_by", length = 300)
    private String lastUpdatedBy;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;
}