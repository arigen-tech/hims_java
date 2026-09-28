package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_crossmatch_hd")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BloodCrossmatchHd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crossmatch_hd_id")
    private Long crossmatchHdId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private BloodRequestHd bloodRequestHd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "crossmatch_type_id", nullable = false)
    private MasCrossMatchType crossmatchType;

    @Column(name = "is_emergency", length = 1)
    private String isEmergency;

    @Column(name = "sample_received_datetime", nullable = false)
    private LocalDateTime sampleReceivedDatetime;

    @Column(name = "crossmatch_datetime", nullable = false)
    private LocalDateTime crossmatchDatetime;

    @Column(name = "overall_result", nullable = false, length = 20)
    private String overallResult;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;
}