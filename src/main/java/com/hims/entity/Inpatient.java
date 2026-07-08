package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "inpatient", uniqueConstraints = {@UniqueConstraint(
        name = "ip_admission_admission_no_key",
                        columnNames = "admission_no")})
public class Inpatient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inpatient_id")
    private Long inpatientId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id")
    private Visit visit;

    @Column(name = "admission_no", unique = true, length = 50)
    @Size(max = 50)
    private String admissionNo;

    @Column(name = "admission_date", nullable = false)
    private LocalDate admissionDate;

    @Column(name = "admission_time", nullable = false)
    private LocalTime admissionTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_type_id")
    private MasAdmissionType admissionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_category_id")
    private MasAdmissionCategory admissionCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_source_id")
    private MasAdmissionSource admissionSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_condition_id")
    private MasPatientCondition patientCondition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "care_level_id")
    private MasCareLevel careLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ward_category_id")
    private MasWardCategory wardCategory;

    @Column(name = "admitting_ward_id")
    private Long admittingWardId;

    @Column(name = "admission_priority", length = 20)
    @Size(max = 20)
    private String admissionPriority;

    @Column(name = "mlc_flag", length = 1)
    private String mlcFlag;

    @Column(name = "vip_flag", length = 1)
    private String vipFlag;

    @Column(name = "initial_diagnosis", length = 300)
    @Size(max = 300)
    private String initialDiagnosis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "icd_diagnosis")
    private MasIcd icdDiagnosis;

    @Column(name = "condition_notes", length = 500)
    @Size(max = 500)
    private String conditionNotes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diet_preference_id")
    private MasDietPreference dietPreference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_status")
    private MasAdmissionStatus admissionStatus;

    @Column(name = "discharge_date")
    private LocalDate dischargeDate;

    @Column(name = "discharge_time")
    private LocalTime dischargeTime;

    @Column(name = "discharge_summary", columnDefinition = "text")
    private String dischargeSummary;

    @Column(name = "discharge_notes", columnDefinition = "text")
    private String dischargeNotes;

    @Column(name = "transfer_hospital_details", length = 500)
    @Size(max = 500)
    private String transferHospitalDetails;

    @Column(name = "date_of_death")
    private LocalDate dateOfDeath;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    @Size(max = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    @Size(max = 200)
    private String lastUpdatedBy;
}