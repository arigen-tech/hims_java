package com.hims.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

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
@DynamicUpdate
@Table(name = "patient_insurance_details", schema = "public")
public class PatientInsuranceDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_insurance_id", nullable = false)
    private Long patientInsuranceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_id", nullable = false)
    private MasInsurance insurance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tpa_id")
    private MasTpa tpa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "corporate_id")
    private MasCorporate corporate;

    @Column(name = "policy_no", length = 100)
    private String policyNo;

    @Column(name = "member_id", length = 100)
    private String memberId;

    @Column(name = "policy_holder_name", length = 200)
    private String policyHolderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relation_id")
    private MasRelation relation;

    @Column(name = "policy_type", length = 50)
    private String policyType;

    @Column(name = "coverage_type", length = 50)
    private String coverageType;

    @Column(name = "sum_insured", precision = 12, scale = 2)
    private BigDecimal sumInsured;

    @Column(name = "balance_sum_insured", precision = 12, scale = 2)
    private BigDecimal balanceSumInsured;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    @Column(name = "card_number", length = 100)
    private String cardNumber;

    @Column(name = "card_image_path", length = 255)
    private String cardImagePath;

    @Column(name = "is_primary", length = 1)
    private String primaryFlag;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}