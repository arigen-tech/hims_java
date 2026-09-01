package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "dental_procedure_tooth")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DentalProcedureTooth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dental_procedure_tooth_id")
    private Long dentalProcedureToothId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_dt_id", referencedColumnName = "procedure_dt_id", nullable = false)
    private ProcedureDt procedureDt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tooth_id", referencedColumnName = "tooth_id",nullable = false)
    private MasToothMaster tooth;

    @Column(name = "tooth_surface", length = 100)
    private String toothSurface;

    @Column(name = "remarks", length = 500)
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