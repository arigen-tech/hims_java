package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ip_discharge_medication", schema = "public")
public class IpDischargeMedication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discharge_medication_id")
    private Long dischargeMedicationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discharge_summary_id")
    private IpDischargeSummary dischargeSummary;

    @Column(name = "medicine_name", nullable = false, length = 255)
    private String medicineName;

    @Column(name = "dosage", length = 50)
    private String dosage;

    @Column(name = "frequency", length = 100)
    private String frequency;

    @Column(name = "duration_days")
    private Integer durationDays;

    @Column(name = "total_doses")
    private Integer totalDoses;

    @Column(name = "route", length = 100)
    private String route;

    @Column(name = "instruction", columnDefinition = "TEXT")
    private String instruction;

    @Column(name = "status", length = 1)
    private String status = "Y";

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}