package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "opd_template_investigation")
public class OpdTemplateInvestigation {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_investigation_id", nullable = false)
    private Long templateInvestigationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private OpdTemplate opdTemplateId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "investigation_id")
    private DgMasInvestigation investigationId;
}
