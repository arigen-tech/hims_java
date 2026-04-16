package com.hims.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "blood_component_inventory" )
public class BloodComponentInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Long inventoryId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_dt_id")
    private BloodDonationDt donationDtId;

    @Column(name = "unit_no", length = 50)
    private String unitNo;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id")
    private MasBloodComponent componentId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_group_id")
    private MasBloodGroup bloodGroupId;

    @Column(name = "volume_ml")
    private Integer volumeMl;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_status")
    private MasBloodInventoryStatus inventoryStatus;

    @Column(name = "reserved_for_patient_id")
    private Long reservedForPatientId;

    @Column(name = "reserved_for_inpatient_id")
    private Long reservedForInpatientId;

    @Column(name = "reservation_datetime")
    private LocalDateTime reservationDatetime;

    @Column(name = "issue_datetime")
    private LocalDateTime issueDatetime;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
    @Column(name = "created_by", length = 200)
    private String createdBy;


}