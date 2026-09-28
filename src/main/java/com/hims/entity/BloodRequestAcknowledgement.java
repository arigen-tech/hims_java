package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_request_acknowledgement")
@Data
public class BloodRequestAcknowledgement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "acknowledgement_id")
    private Long acknowledgementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_hd_id", nullable = false)
    private BloodRequestHd bloodRequestHd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_dt_id", nullable = false)
    private BloodRequestDt bloodRequestDt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allocation_id", nullable = false)
    private BloodRequestDtAllocation allocation;

    @Column(name = "acknowledgement_status", nullable = false)
    private String acknowledgementStatus;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "acknowledged_by")
    private Long acknowledgedBy;

    @Column(name = "acknowledged_date")
    private LocalDateTime acknowledgedDate;
}