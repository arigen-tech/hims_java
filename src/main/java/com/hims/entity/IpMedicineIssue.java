package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ip_medicine_issue", schema = "public")
public class IpMedicineIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ip_medicine_issue_seq")
    @SequenceGenerator(
            name = "ip_medicine_issue_seq",
            sequenceName = "ip_medicine_issue_ip_medicine_issue_id_seq",
            allocationSize = 1
    )
    @Column(name = "ip_medicine_issue_id", nullable = false)
    private Long ipMedicineIssueId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private IpMedicinePrescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mar_id", nullable = false)
    private IpMarDetails marDetails;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private MasStoreItem item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private StoreItemBatchStock batch;

    @Column(name = "batch_no", length = 50)
    private String batchNo;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "issue_qty", nullable = false, precision = 12, scale = 2)
    private BigDecimal issueQty;

    @Column(name = "issue_datetime", nullable = false)
    private LocalDateTime issueDatetime;

    @Column(name = "issued_by", nullable = false)
    private Long issuedBy;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "last_chg_by")
    private Long lastChgBy;

    @Column(name = "last_chg_on")
    private LocalDateTime lastChgOn;

    @Column(name = "remarks", length = 250)
    private String remarks;

}