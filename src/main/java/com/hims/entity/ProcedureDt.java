package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "procedure_dt")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureDt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "procedure_dt_id")
    private Long procedureDtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_hd_id",referencedColumnName = "procedure_hd_id",nullable = false)
    private ProcedureHd procedureHd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", referencedColumnName = "procedure_id", nullable = false)
    private MasProcedure procedure;

    @Column(name = "sequence_no", nullable = false)
    private Integer sequenceNo;

    @Column(name = "planned_session_count", nullable = false)
    private Integer plannedSessionCount;

    @Column(name = "completed_session_count", nullable = false)
    private Integer completedSessionCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_status_id", referencedColumnName = "procedure_status_id", nullable = false)
    private MasProcedureStatus procedureStatus;

    @Column(name = "billing_method", length = 20, nullable = false)
    private String billingMethod;

    @Column(name = "billing_status", length = 1, nullable = false)
    private String billingStatus;

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