package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "billing_details")
public class BillingDetail {
    @Id
    @Column(name = "detail_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "billing_hd_id", nullable = false)
    private BillingHeader billingHd;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigation_id", nullable = false)
    private DgMasInvestigation investigation;

    @NotNull
    @Column(name = "charge_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal chargeCost;

    @Column(name = "discount", precision = 10, scale = 2)
    private BigDecimal discount;

    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @Column(name = "created_dt")
    private Instant createdDt;

    @Column(name = "updated_dt")
    private Instant updatedDt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private DgInvestigationPackage packageField;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opd_service_id")
    private MasServiceOpd opdService;

}
