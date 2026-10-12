package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "procedure_session")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcedureSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "procedure_session_id")
    private Long procedureSessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_dt_id",referencedColumnName = "procedure_dt_id",nullable = false)
    private ProcedureDt procedureDt;

    @Column(name = "session_no", nullable = false)
    private Integer sessionNo;

    @Column(name = "scheduled_date_time")
    private LocalDateTime scheduledDateTime;

    @Column(name = "performed_date_time")
    private LocalDateTime performedDateTime;

    @Column(name = "performed_by", length = 200)
    private String performedBy;

    @Column(name = "session_status", length = 20, nullable = false)
    private String sessionStatus;

    @Column(name = "is_final_session", length = 1, nullable = false)
    private String isFinalSession;

    @Column(name = "billing_required", length = 1, nullable = false)
    private String billingRequired;

    @Column(name = "billing_status", length = 20, nullable = false)
    private String billingStatus;

    @Column(name = "billing_reference_id")
    private Long billingReferenceId;

    @Column(name = "procedure_findings", length = 2000)
    private String procedureFindings;

    @Column(name = "procedure_notes", length = 2000)
    private String procedureNotes;

    @Column(name = "complication_flag", length = 1, nullable = false)
    private String complicationFlag;

    @Column(name = "complication_details", length = 1000)
    private String complicationDetails;

    @Column(name = "post_procedure_advice", length = 2000)
    private String postProcedureAdvice;

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