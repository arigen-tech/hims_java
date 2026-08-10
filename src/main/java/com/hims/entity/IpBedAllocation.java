package com.hims.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ip_bed_allocation", schema = "public")
public class IpBedAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bed_allocation_id")
    private Long bedAllocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ip_admission_id")
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_id")
    private MasWard ward;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private MasRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bed_id")
    private MasBed bed;

    @Column(name = "allocation_start_date")
    private LocalDateTime allocationStartDate;

    @Column(name = "allocation_end_date")
    private LocalDateTime allocationEndDate;

    @Column(name = "allocation_status", length = 20)
    private String allocationStatus;

    @Column(name = "reason", length = 300)
    private String reason;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}