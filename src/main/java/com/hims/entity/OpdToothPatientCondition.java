package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "opd_tooth_patient_condition")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpdToothPatientCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_tooth_condition_id")
    private Long patientToothConditionId;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "visit_id", nullable = false)
    private Long visitId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tooth_id", nullable = false)
    private MasToothMaster tooth;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condition_id", nullable = false)
    private MasToothCondition condition;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "status", columnDefinition = "bpchar(1)", nullable = false)
    private String status;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_updated_by", length = 100)
    private String lastUpdatedBy;

    @Column(name = "last_update_date", nullable = false)
    private LocalDateTime lastUpdateDate;
}