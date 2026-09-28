package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "blood_crossmatch_failed_history")
public class BloodCrossmatchFailedHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "failed_history_id", nullable = false)
    private Long failedHistoryId;

    @ManyToOne
    @JoinColumn(name = "blood_detail_id", nullable = false)
    private BloodRequestDt bloodRequestDt;

    @ManyToOne
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @ManyToOne
    @JoinColumn(name = "inventory_id", nullable = false)
    private BloodComponentInventory inventory;

    @Column(name = "failed_date", nullable = false)
    private LocalDateTime failedDate;

    @Column(name = "submitted_by", nullable = false)
    private Long submittedBy;

    @Column(name = "remarks", nullable = false, length = 500)
    private String remarks;
}