package com.hims.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "mas_ipd_package_inclusion", schema = "public")
public class MasIpdPackageInclusion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inclusion_id")
    private Long inclusionId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private MasIpdPackage masIpdPackage;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_category_id")
    private MasIpdServiceCategory serviceCategoryId;

    @Column(name = "included_flag", length = 1)
    private String includedFlag;

    @Column(name = "limit_amount", precision = 12, scale = 2)
    private BigDecimal limitAmount;

    @Column(name = "limit_qty")
    private Integer limitQty;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_updated_by", length = 100)
    private String lastUpdatedBy;

    @Column(name = "last_updated_date")
    private LocalDateTime lastUpdatedDate;

    @Column(name = "status", length = 1)
    private String status;

}