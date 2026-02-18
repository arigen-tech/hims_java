package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "rad_orderdt")
public class RadOrderDt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rad_orderdt_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "rad_orderhd_id", nullable = false)
    private RadOrderHd radOrderhd;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "investigation_id", nullable = false)
    private DgMasInvestigation investigation;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sub_chargecode_id", nullable = false)
    private MasSubChargeCode subChargecode;

    @Size(max = 50)
    @NotNull
    @Column(name = "order_accession_no", nullable = false, length = 50)
    private String orderAccessionNo;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate;

    @Column(name = "study_status", length = Integer.MAX_VALUE)
    private String studyStatus;

    @Size(max = 255)
    @Column(name = "study_failure_reason")
    private String studyFailureReason;

    @Column(name = "report_status", length = Integer.MAX_VALUE)
    private String reportStatus;

    @Size(max = 255)
    @Column(name = "report_failure_reason")
    private String reportFailureReason;

    @Column(name = "hl7_mwl_status", length = Integer.MAX_VALUE)
    private String hl7MwlStatus;

    @Size(max = 255)
    @Column(name = "hl7_failure_reason")
    private String hl7FailureReason;

    @Column(name = "pacs_completion_status", length = Integer.MAX_VALUE)
    private String pacsCompletionStatus;

    @Size(max = 255)
    @Column(name = "pacs_failure_reason")
    private String pacsFailureReason;

    @Column(name = "billing_status", length = Integer.MAX_VALUE)
    private String billingStatus;

    @Size(max = 200)
    @Column(name = "createdby", length = 200)
    private String createdby;

    @Column(name = "createdon")
    private Instant createdon;

    @Size(max = 200)
    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id",nullable = true)
    private DgInvestigationPackage packageId;

    @Size(max = 1)
    @Column(name = "order_status", length = 1)
    private String orderStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_hd_id")
    private BillingHeader billingHd;

}
