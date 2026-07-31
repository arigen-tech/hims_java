package com.hims.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ip_diagnosis_entry", schema = "public")
public class IpDiagnosisEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "diagnosis_id", nullable = false)
    private Long diagnosisId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private MasDepartment department;

    @Column(name = "diagnosis_type", length = 10)
    private String diagnosisType;

    @Column(name = "diagnosis_text", length = 500)
    private String diagnosisText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icd_id")
    private MasIcd icd;

    @Column(name = "status", length = 15)
    private String status;

    @Column(name = "diagnosis_datetime")
    private LocalDateTime diagnosisDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}