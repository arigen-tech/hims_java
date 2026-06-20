package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "opd_ent_details")
@Data
public class OpdEntDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ent_id")
    private Long entId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;


    @Column(name = "opd_date")
    private LocalDate opdDate;

    @Column(name = "right_pinna", length = 100)
    private String rightPinna;

    @Column(name = "left_pinna", length = 100)
    private String leftPinna;

    @Column(name = "right_ear_canal", length = 100)
    private String rightEarCanal;

    @Column(name = "left_ear_canal", length = 100)
    private String leftEarCanal;

    @Column(name = "right_tm_status", length = 100)
    private String rightTmStatus;

    @Column(name = "left_tm_status", length = 100)
    private String leftTmStatus;

    @Column(name = "rinne_test", length = 50)
    private String rinneTest;

    @Column(name = "weber_test", length = 50)
    private String weberTest;

    @Column(name = "abc_test", length = 200)
    private String abcTest;

    @Column(name = "audiometry_findings", length = 500)
    private String audiometryFindings;

    @Column(name = "external_nose", length = 300)
    private String externalNose;

    @Column(name = "nasal_mucosa", length = 50)
    private String nasalMucosa;

    @Column(name = "septum", length = 50)
    private String septum;

    @Column(name = "turbinates", length = 200)
    private String turbinates;

    @Column(name = "nasal_polyp", length = 10)
    private String nasalPolyp;

    @Column(name = "nasal_discharge", length = 100)
    private String nasalDischarge;

    @Column(name = "maxillary_tenderness", length = 20)
    private String maxillaryTenderness;

    @Column(name = "frontal_tenderness", length = 20)
    private String frontalTenderness;

    @Column(name = "oral_cavity", length = 500)
    private String oralCavity;

    @Column(name = "tonsil_grade", length = 50)
    private String tonsilGrade;

    @Column(name = "tonsil_congestion", length = 10)
    private String tonsilCongestion;

    @Column(name = "tonsil_follicles", length = 10)
    private String tonsilFollicles;

    @Column(name = "tonsil_membrane", length = 10)
    private String tonsilMembrane;

    @Column(name = "peritonsillar_abscess", length = 10)
    private String peritonsillarAbscess;

    @Column(name = "pharynx", length = 500)
    private String pharynx;

    @Column(name = "uvula", length = 20)
    private String uvula;

    @Column(name = "voice_quality", length = 50)
    private String voiceQuality;

    @Column(name = "thyroid_enlargement", length = 10)
    private String thyroidEnlargement;

    @Column(name = "cervical_nodes", length = 300)
    private String cervicalNodes;

    @Column(name = "neck_mass", length = 10)
    private String neckMass;

    @Column(name = "neck_other_findings", length = 300)
    private String neckOtherFindings;

    @Column(name = "status", nullable = false, length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}