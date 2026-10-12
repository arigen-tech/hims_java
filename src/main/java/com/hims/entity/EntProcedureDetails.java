package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "ent_procedure_details")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntProcedureDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ent_procedure_detail_id")
    private Long entProcedureDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "procedure_dt_id",
            referencedColumnName = "procedure_dt_id",
            nullable = false
    )
    private ProcedureDt procedureDt;

    @Column(name = "site_type", length = 30, nullable = false)
    private String siteType;

    @Column(name = "side", length = 20)
    private String side;

    @Column(name = "specific_site", length = 200)
    private String specificSite;

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