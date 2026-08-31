package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "obg_procedure_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ObgProcedureDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "obg_procedure_detail_id")
    private Long obgProcedureDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "procedure_dt_id",
            referencedColumnName = "procedure_dt_id",
            nullable = false
    )
    private ProcedureDt procedureDt;

    @Column(name = "lmp_date")
    private LocalDate lmpDate;

    @Column(name = "pregnancy_status", length = 20)
    private String pregnancyStatus;

    @Column(name = "gestational_age_weeks")
    private Integer gestationalAgeWeeks;

    @Column(name = "specimen_collected", length = 1, nullable = false)
    private String specimenCollected;

    @Column(name = "specimen_type", length = 100)
    private String specimenType;

    @Column(name = "device_type", length = 100)
    private String deviceType;

    @Column(name = "device_batch_no", length = 100)
    private String deviceBatchNo;

    @Column(name = "remarks", length = 1000)
    private String remarks;

    @Column(name = "status", length = 1, nullable = false)
    private String status;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @Column(name = "last_update_date", nullable = false)
    private LocalDateTime lastUpdateDate;
}