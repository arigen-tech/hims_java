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
@Table(name = "ip_mar_details", schema = "public")
public class IpMarDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mar_id", nullable = false)
    private Long marId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false)
    private IpMedicinePrescription prescription;

    @Column(name = "administered_qty", nullable = false, precision = 10, scale = 2)
    private BigDecimal administeredQty;

    @Column(name = "administration_time", nullable = false)
    private LocalDateTime administrationTime;

    @Column(name = "administered_by", length = 100)
    private String administeredBy;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "batch_no", length = 100)
    private String batchNo;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "is_billed")
    private Boolean isBilled;

    @Column(name = "bill_item_id")
    private Long billItemId;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;



}