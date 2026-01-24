package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_policy_master", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingPolicyMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_policy_id")
    private Long billingPolicyId;

    @Column(name = "policy_code", length = 50)
    private String policyCode;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "applicable_billing_type", length = 20)
    private String applicableBillingType;

    @Column(name = "followup_days_allowed")
    private Integer followupDaysAllowed;

    @Column(name = "discount_percentage")
    private BigDecimal discountPercentage;

//    @Column(name = "status", length = 1)
//    private String status;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;
}
