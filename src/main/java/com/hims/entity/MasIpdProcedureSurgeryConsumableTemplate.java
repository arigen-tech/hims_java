package com.hims.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "mas_ipd_procedure_surgery_consmble_template", schema = "public")
public class MasIpdProcedureSurgeryConsumableTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id")
    private Long templateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedure_id", insertable = false, updatable = false)
    private MasProcedure procedure;

    @Column(name = "template_name", length = 200)
    private String templateName;

    @Column(name = "is_default")
    private Boolean isDefault;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "surgery_id")
    private MasSurgery surgery;

    @Column(name = "template_type", length = 20)
    private String templateType;
}