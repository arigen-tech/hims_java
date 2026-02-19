package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;


@Entity
@Data
@Table(name = "opd_template")
public class OpdTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Size(max = 32)
    @Column(name = "template_code", length = 32)
    private String opdTemplateCode;

    @Size(max = 200)
    @Column(name = "template_name", length = 200)
    private String opdTemplateName;

    @Size(max = 1)
    @Column(name = "template_type", length = 1)
    private String opdTemplateType;

    @Size(max = 200)
    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Size(max = 1)
    @Column(name = "status", length = 1)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private MasDepartment departmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctorId;
}
