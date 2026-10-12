package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_procedure_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasProcedureStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "procedure_status_id")
    private Long procedureStatusId;

    @Column(name = "status_code", length = 30, nullable = false)
    private String statusCode;

    @Column(name = "status_name", length = 100, nullable = false)
    private String statusName;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "status", length = 1, nullable = false)
    private String status;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "last_updated_by", length = 100)
    private String lastUpdatedBy;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;
}