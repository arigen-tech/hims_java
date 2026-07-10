package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "insurance_tpa_mapping", schema = "public")
public class InsuranceTpaMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mapping_id")
    private Long mappingId;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "insurance_id")
    private MasInsurance insurance;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "tpa_id")
    private MasTpa tpa;

    @Column(name = "effective_from")
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "mode", length = 30)
    private String mode;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}