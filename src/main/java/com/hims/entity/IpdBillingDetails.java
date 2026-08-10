package com.hims.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.SourceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@DynamicInsert
@Table(name = "ipd_billing_details", schema = "public")
public class IpdBillingDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_item_id", nullable = false)
    private Long billItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private IpdBillingHeader billHeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private MasIpdServiceCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id")
    private MasIpdServiceSubcategory subcategory;

    @JoinColumn(name = "source_type")
    private String  sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "item_name", length = 300)
    private String itemName;

    @Column(name = "service_date")
    private LocalDateTime serviceDate;

    @Column(name = "quantity", precision = 10, scale = 2)
    private BigDecimal quantity;


    @Column(name = "rate", precision = 12, scale = 2)
    private BigDecimal rate;


    @Column(name = "amount", precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "gst_percent", precision = 5, scale = 2)
    private BigDecimal gstPercent;


    @Column(name = "gst_amount", precision = 12, scale = 2)
    private BigDecimal gstAmount = BigDecimal.ZERO;


    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;


    @Column(name = "net_amount", precision = 12, scale = 2)
    private BigDecimal netAmount;


    @Column(name = "is_package_item")
    private Boolean packageItem;


    @Column(name = "is_part_of_package")
    private Boolean partOfPackage;

    @Column(name = "package_id")
    private Long packageId;


    @Column(name = "is_payable")
    private Boolean payable;

    @Column(name = "approved_amount", precision = 12, scale = 2)
    private BigDecimal approvedAmount;


    @Column(name = "deduction_amount", precision = 12, scale = 2)
    private BigDecimal deductionAmount = BigDecimal.ZERO;

    @Column(name = "deduction_reason", length = 255)
    private String deductionReason;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_cancelled")
    private Boolean cancelled ;

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @Column(name = "item_status", length = 20)
    private String itemStatus;

    @Column(name = "reference_no", length = 100)
    private String referenceNo;

    @Column(name = "remarks", length = 255)
    private String remarks;


    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

}