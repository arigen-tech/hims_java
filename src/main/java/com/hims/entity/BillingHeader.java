package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "billing_header")
public class BillingHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bill_hd_id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "bill_no", nullable = false, length = 50)
    private String billNo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Size(max = 255)
    @Column(name = "patient_display_name")
    private String patientDisplayName;

    @Column(name = "patient_age")
    private String patientAge;

    @Size(max = 10)
    @Column(name = "patient_gender", length = 10)
    private String patientGender;

    @Size(max = 500)
    @Column(name = "patient_address", length = 500)
    private String patientAddress;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private MasHospital hospital;

    @Size(max = 255)
    @Column(name = "hospital_name")
    private String hospitalName;

    @Size(max = 500)
    @Column(name = "hospital_address", length = 500)
    private String hospitalAddress;

    @Size(max = 20)
    @Column(name = "hospital_mobile_no", length = 20)
    private String hospitalMobileNo;

    @Size(max = 15)
    @Column(name = "hospital_gstin", length = 15)
    private String hospitalGstin;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_category_id", nullable = false)
    private MasServiceCategory serviceCategory;

    @Size(max = 100)
    @Column(name = "referred_by", length = 100)
    private String referredBy;

    @Size(max = 50)
    @Column(name = "gstn_bill_no", length = 50)
    private String gstnBillNo;

    @NotNull
    @Column(name = "billing_date", nullable = false)
    private Instant billingDate;

    @Column(name = "total_amount", precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "discount_amount", precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "tax_total", precision = 10, scale = 2)
    private BigDecimal taxTotal;

    @Column(name = "net_amount", precision = 10, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "total_paid", precision = 10, scale = 2)
    private BigDecimal totalPaid;

    @Size(max = 20)
    @Column(name = "payment_status", length = 20)
    private String paymentStatus;

    @Size(max = 150)
    @Column(name = "created_by", length = 150)
    private String createdBy;

    @Column(name = "created_dt")
    private Instant createdDt;

    @Column(name = "updated_dt")
    private Instant updatedDt;


    @Column(name = "billing_hd_id")//, nullable = false
    private Integer billingHdId;

    @Column(name = "bill_date")
    private OffsetDateTime billDate;

    @Size(max = 100)
    @Column(name = "invoice_no", length = 100)
    private String invoiceNo;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_id")
    private MasDiscount discount;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hdorder_id")
    private DgOrderHd hdorder;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @Column(name = "registration_cost")
    private BigDecimal registrationCost;

}
