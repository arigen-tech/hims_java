package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "ip_discharge_summary", schema = "public")
public class IpDischargeSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discharge_id")
    private Long dischargeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inpatient_id", nullable = false)
    private Inpatient inpatient;

    @Column(name = "discharge_date", nullable = false)
    private LocalDateTime dischargeDate;

    @Column(name = "consultant_name", length = 200)
    private String consultantName;

    @Column(name = "primary_diagnosis", length = 255)
    private String primaryDiagnosis;

    @Column(name = "secondary_diagnosis", length = 255)
    private String secondaryDiagnosis;

    @Column(name = "presenting_complaints", columnDefinition = "TEXT")
    private String presentingComplaints;

    @Column(name = "history_of_illness", columnDefinition = "TEXT")
    private String historyOfIllness;

    @Column(name = "past_history", columnDefinition = "TEXT")
    private String pastHistory;

    @Column(name = "examination_findings", columnDefinition = "TEXT")
    private String examinationFindings;

    @Column(name = "procedure_details", columnDefinition = "TEXT")
    private String procedureDetails;

    @Column(name = "hospital_course", columnDefinition = "TEXT")
    private String hospitalCourse;

    @Column(name = "investigation_summary", columnDefinition = "TEXT")
    private String investigationSummary;

    @Column(name = "treatment_summary", columnDefinition = "TEXT")
    private String treatmentSummary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_id")
    private MasPatientDischargeCondition condition;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discharge_reason_id")
    private MasDischargeReason dischargeReason;

    @Column(name = "discharged_to", length = 50)
    private String dischargedTo;

    @Column(name = "referred_hospital_name", length = 255)
    private String referredHospitalName;

    @Column(name = "discharge_medications", columnDefinition = "TEXT")
    private String dischargeMedications;

    @Column(name = "discharge_advice", columnDefinition = "TEXT")
    private String dischargeAdvice;

    @Column(name = "follow_up_advice", columnDefinition = "TEXT")
    private String followUpAdvice;

    @Column(name = "status", length = 1)
    private String status = "Y";

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}