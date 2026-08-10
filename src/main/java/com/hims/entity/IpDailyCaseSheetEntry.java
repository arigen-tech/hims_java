package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ip_daily_case_sheet_entry")
public class IpDailyCaseSheetEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "case_sheet_entry_id")
    private Long caseSheetEntryId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "inpatient_id", nullable = false, foreignKey = @ForeignKey(name = "ip_daily_case_sheet_entry_inpatient_id_fkey"))
    private Inpatient inpatient;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false, foreignKey = @ForeignKey(name = "ip_daily_case_sheet_entry_patient_id_fkey"))
    private Patient patient;

    @Column(name = "visit_datetime", nullable = false)
    private LocalDateTime visitDatetime;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "doctor_id", nullable = false, foreignKey = @ForeignKey(name = "ip_daily_case_sheet_entry_doctor_id_fkey"))
    private User doctor;

    @Column(name = "doctor_name", nullable = false, length = 150)
    private String doctorName;

    @Column(name = "doctor_role", length = 50)
    private String doctorRole;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "visit_department_id", nullable = false, foreignKey = @ForeignKey(name = "ip_daily_case_sheet_entry_visit_department_id_fkey"))
    private MasDepartment visitDepartment;

    @Column(name = "doctor_notes", columnDefinition = "TEXT")
    private String doctorNotes;

    @Column(name = "investigation_summary", columnDefinition = "TEXT")
    private String investigationSummary;

    @Column(name = "medicine_summary", columnDefinition = "TEXT")
    private String medicineSummary;

    @Column(name = "procedure_summary", columnDefinition = "TEXT")
    private String procedureSummary;

    @Column(name = "care_plan_changes", columnDefinition = "TEXT")
    private String carePlanChanges;

    @Column(name = "next_follow_up_plan", length = 300)
    private String nextFollowUpPlan;

    @Column(name = "is_finalized", length = 1)
    private String isFinalized;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_type_id")
    private MasVisitType visitType;

}