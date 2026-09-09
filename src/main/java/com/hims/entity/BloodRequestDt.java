package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_request_dt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BloodRequestDt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_dt_id")
    private Long requestDtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_hd_id", nullable = false)
    private BloodRequestHd bloodRequestHd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id", nullable = false)
    private MasBloodComponent component;

    @Column(name = "units_required", nullable = false)
    private Integer unitsRequired;

    @Column(name = "required_by_datetime", nullable = false)
    private LocalDateTime requiredByDatetime;

    @Column(name = "fulfilled_units")
    private Integer fulfilledUnits = 0;

    @Column(name = "detail_status", nullable = false, length = 30)
    private String detailStatus;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "urgency", nullable = false, length = 20)
    private String urgency;

    @Column(name = "clinical_indication", length = 200)
    private String clinicalIndication;

    @Column(name = "remarks", length = 200)
    private String remarks;
}