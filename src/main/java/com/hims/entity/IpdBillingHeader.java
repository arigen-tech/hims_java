package com.hims.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "ipd_billing_header", schema = "public")
public class IpdBillingHeader {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_id", nullable = false)
    private Long billId;

    @Column(name = "uhid", nullable = false, length = 50)
    private String uhid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatientId;

    @Column(name = "patient_name", length = 150)
    private String patientName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_type_id", nullable = false)
    private MasIpdBillingType billingType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insurance_id")
    private MasInsurance insurance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tpa_id")
    private MasTpa tpa;

    @Column(name = "policy_no", length = 100)
    private String policyNo;

    @Column(name = "claim_no", length = 100)
    private String claimNo;

    @Column(name = "credit_limit", precision = 12, scale = 2)
    private BigDecimal creditLimit;


    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;


    @Column(name = "gst_amount", precision = 12, scale = 2)
    private BigDecimal gstAmount;


    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount;


    @Column(name = "net_amount", precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "insurance_payable_amount", precision = 12, scale = 2)
    private BigDecimal insurancePayableAmount;


    @Column(name = "patient_payable_amount", precision = 12, scale = 2)
    private BigDecimal patientPayableAmount ;


    @Column(name = "non_payable_amount", precision = 12, scale = 2)
    private BigDecimal nonPayableAmount ;


    @Column(name = "insurance_approved_amount", precision = 12, scale = 2)
    private BigDecimal insuranceApprovedAmount ;

    @Column(name = "insurance_settled_amount", precision = 12, scale = 2)
    private BigDecimal insuranceSettledAmount;


    @Column(name = "patient_paid_amount", precision = 12, scale = 2)
    private BigDecimal patientPaidAmount ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_status_id")
    private MasIpdBillStatus billStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_status_id")
    private MasIpdPaymentStatus paymentStatus;

    @Column(name = "bill_date")
    private LocalDateTime billDate;

    @Column(name = "remarks", columnDefinition = "text")
    private String remarks;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;


    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "preauth_id")
//    private TpaPreauthRequest preauth;


    @Column(name = "patient_refund_amount", precision = 12, scale = 2)
    private BigDecimal patientRefundAmount;


    @Column(name = "refund_status", length = 1)
    private String refundStatus;
}