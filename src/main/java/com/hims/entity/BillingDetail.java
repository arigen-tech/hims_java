package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "billing_details")
public class BillingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_dt_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bill_hd_id", nullable = false)
    private BillingHeader billHd;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_category_id", nullable = false)
    private MasServiceCategory serviceCategory;

    @NotNull
    @Column(name = "service_id", nullable = false)
    private Long serviceId;

    @Size(max = 500)
    @Column(name = "item_name", length = 500)
    private String itemName;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "base_price", precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "tariff", precision = 10, scale = 2)
    private BigDecimal tariff;

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(name = "amount_after_discount", precision = 10, scale = 2)
    private BigDecimal amountAfterDiscount;

    @Column(name = "tax_percent", precision = 5, scale = 2)
    private BigDecimal taxPercent;

    @Column(name = "tax_amount", precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "net_amount", precision = 10, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "created_at")
    private Instant createdAt;

    @NotNull
    @Column(name = "detail_id", nullable = false)
    private Integer detailId;

    @NotNull
    @Column(name = "charge_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal chargeCost;

    @Column(name = "created_dt")
    private OffsetDateTime createdDt;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "updated_dt")
    private OffsetDateTime updatedDt;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "billing_hd_id", nullable = false)
    private BillingHeader billingHd;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigation_id", nullable = false)
    private DgMasInvestigation investigation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opd_service_id")
    private MasServiceOpd opdService;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private DgInvestigationPackage packageField;

}
