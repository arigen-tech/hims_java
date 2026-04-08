package com.hims.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "package_rate_config", schema = "public")
public class PackageRateConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "config_id")
    private Long configId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private MasIpdPackage ipdPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_type_id")
    private MasIpdBillingType billingType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_id")
    private MasInsurance insuranceId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tpa_id")
    private MasTpa tpa;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporate_id")
    private MasCorporate corporate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_category_id")
    private MasRoomCategory masRoomCategory;

    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "preauth_required", length = 1)
    private String preauthRequired;

    @Column(name = "copay_percent", precision = 5, scale = 2)
    private BigDecimal copayPercent;

    @Column(name = "max_claim_amount", precision = 12, scale = 2)
    private BigDecimal maxClaimAmount;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "last_updated_by", length = 100)
    private String lastUpdatedBy;

    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;

}
