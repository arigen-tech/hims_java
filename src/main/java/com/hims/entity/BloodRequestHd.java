package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_request_hd")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BloodRequestHd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_hd_id")
    private Long requestHdId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_ward_id")
    private MasWard masWard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_group_id", nullable = false)
    private MasBloodGroup bloodGroup;

    @Column(name = "request_datetime", nullable = false)
    private LocalDateTime requestDatetime;

    @Column(name = "requested_by", nullable = false, length = 200)
    private String requestedBy;

    @Column(name = "overall_status", nullable = false, length = 30)
    private String overallStatus;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;
}
