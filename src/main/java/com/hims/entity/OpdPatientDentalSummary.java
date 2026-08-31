package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "opd_patient_dental_summary")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpdPatientDentalSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "summary_id")
    private Long summaryId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "visit_id", nullable = false)
    private Long visitId;

    @Column(name = "total_teeth")
    private Integer totalTeeth;

    @Column(name = "missing_teeth")
    private Integer missingTeeth;

    @Column(name = "unsalvageable_teeth")
    private Integer unsalvageableTeeth;

    @Column(name = "affected_teeth")
    private Integer affectedTeeth;

    @Column(name = "dental_disease_score")
    private Integer dentalDiseaseScore;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "status", columnDefinition = "bpchar(1)")
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @Column(name = "ongoing_procedures")
    private Integer ongoingProcedures;
}
