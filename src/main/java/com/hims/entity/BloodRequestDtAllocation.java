package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_request_dt_allocation")
@Getter
@Setter
public class BloodRequestDtAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "allocation_id")
    private Long allocationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_dt_id", nullable = false)
    private BloodRequestDt bloodRequestDt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private BloodComponentInventory inventory;

    @Column(name = "allocated_units")
    private Integer allocatedUnits;

    @Column(name = "allocated_date")
    private LocalDateTime allocatedDate;

    @Column(name = "created_by")
    private String createdBy;
}