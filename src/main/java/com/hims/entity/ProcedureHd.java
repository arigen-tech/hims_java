package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "procedure_hd")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureHd {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "procedure_hd_id")
    private Long procedureHdId;

    @Column(name = "procedure_no", length = 50, nullable = false)
    private String procedureNo;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "visit_id", nullable = false)
    private Long visitId;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    @Column(name = "advised_by", length = 200, nullable = false)
    private String advisedBy;

    @Column(name = "advised_date", nullable = false)
    private LocalDateTime advisedDate;

    @Column(name = "diagnosis", length = 1000)
    private String diagnosis;

    @Column(name = "priority", length = 20, nullable = false)
    private String priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_status_id", referencedColumnName = "procedure_status_id",nullable = false)
    private MasProcedureStatus procedureStatus;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_type_id", referencedColumnName = "procedure_type_id", nullable = false)
    private MasProcedureType procedureType;

    @Column(name = "payment_status", length = 1, nullable = false)
    private String paymentStatus;
}