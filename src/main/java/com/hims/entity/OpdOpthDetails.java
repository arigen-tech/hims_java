package com.hims.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "opd_opth_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpdOpthDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opd_id")
    private Long opdId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @Column(name = "opd_date",length = 20)
    private LocalDate opdDate;

    // -------- Distance Vision --------
    @Column(name = "re_distance_unaided",length = 20)
    private String reDistanceUnaided;

    @Column(name = "re_distance_pinhole",length = 20)
    private String reDistancePinhole;

    @Column(name = "re_distance_best_corrected",length = 20)
    private String reDistanceBestCorrected;

    @Column(name = "le_distance_unaided",length = 20)
    private String leDistanceUnaided;

    @Column(name = "le_distance_pinhole",length = 20)
    private String leDistancePinhole;

    @Column(name = "le_distance_best_corrected",length = 20)
    private String leDistanceBestCorrected;

    // -------- Near Vision --------
    @Column(name = "re_near_unaided",length = 20)
    private String reNearUnaided;

    @Column(name = "re_near_pinhole",length = 20)
    private String reNearPinhole;

    @Column(name = "re_near_best_corrected",length = 20)
    private String reNearBestCorrected;

    @Column(name = "le_near_unaided",length = 20)
    private String leNearUnaided;

    @Column(name = "le_near_pinhole",length = 20)
    private String leNearPinhole;

    @Column(name = "le_near_best_corrected",length = 20)
    private String leNearBestCorrected;

    // -------- Fundus --------
    @Column(name = "fundus_glow",length = 10)
    private String fundusGlow;



    // -------- Retinoscopy --------
    @Column(name = "re_retinoscopy_axis",length = 10)
    private String reRetinoscopyAxis;

    @Column(name = "re_retinoscopy_v",length = 10)
    private String reRetinoscopyV;

    @Column(name = "re_retinoscopy_h",length = 10)
    private String reRetinoscopyH;

    @Column(name = "le_retinoscopy_axis",length = 10)
    private String leRetinoscopyAxis;

    @Column(name = "le_retinoscopy_v",length = 10)
    private String leRetinoscopyV;

    @Column(name = "le_retinoscopy_h",length = 10)
    private String leRetinoscopyH;

    // -------- Measurements --------
    @Column(name = "re_keratometry",length = 20)
    private String reKeratometry;

    @Column(name = "re_pachymetry",length = 20)
    private String rePachymetry;

    @Column(name = "re_tonometry",length = 20)
    private String reTonometry;

    @Column(name = "re_field_of_vision",length = 20)
    private String reFieldOfVision;

    @Column(name = "re_iol_power",length = 20)
    private String reIolPower;

    @Column(name = "le_keratometry",length = 20)
    private String leKeratometry;

    @Column(name = "le_pachymetry",length = 20)
    private String lePachymetry;

    @Column(name = "le_tonometry",length = 20)
    private String leTonometry;

    @Column(name = "le_field_of_vision",length = 50)
    private String leFieldOfVision;

    @Column(name = "le_iol_power",length = 20)
    private String leIolPower;

    // -------- Refraction Distance --------
    @Column(name = "re_sph_dist",length = 10)
    private String reSphDist;

    @Column(name = "re_cyl_dist",length = 10)
    private String reCylDist;

    @Column(name = "re_axis_dist",length = 10)
    private String reAxisDist;

    @Column(name = "le_sph_dist",length = 10)
    private String leSphDist;

    @Column(name = "le_cyl_dist",length = 10)
    private String leCylDist;

    @Column(name = "le_axis_dist",length = 10)
    private String leAxisDist;

    // -------- Refraction Near --------
    @Column(name = "re_sph_near",length = 10)
    private String reSphNear;

    @Column(name = "re_cyl_near",length = 10)
    private String reCylNear;

    @Column(name = "re_axis_near",length = 10)
    private String reAxisNear;

    @Column(name = "le_sph_near",length = 10)
    private String leSphNear;

    @Column(name = "le_cyl_near",length = 10)
    private String leCylNear;

    @Column(name = "le_axis_near",length = 10)
    private String leAxisNear;

    // -------- General --------
    @Column(name = "ipd_value",length = 10)
    private String ipdValue;

    @Column(name = "spectacle_use",length = 50)
    private String spectacleUse;

    @Column(name = "lens_type",length = 50)
    private String lensType;

    // -------- External Eye (RE) --------
    @Column(name = "re_eyebrow",length = 50)
    private String reEyebrow;
    @Column(name = "re_eyelid",length = 50)
    private String reEyelid;
    @Column(name = "re_cornea",length = 50)
    private String reCornea;
    @Column(name = "re_conjunctiva",length = 50)
    private String reConjunctiva;
    @Column(name = "re_fornix",length = 50)
    private String reFornix;
    @Column(name = "re_limbus",length = 50)
    private String reLimbus;
    @Column(name = "re_sclera",length = 50)
    private String reSclera;
    @Column(name = "re_anterior_chamber",length = 50)
    private String reAnteriorChamber;
    @Column(name = "re_iris",length = 50)
    private String reIris;
    @Column(name = "re_pupil",length = 50)
    private String rePupil;
    @Column(name = "re_lens",length = 50)
    private String reLens;
    @Column(name = "re_vitreous_anterior",length = 50)
    private String reVitreousAnterior;

    // -------- External Eye (LE) --------
    @Column(name = "le_eyebrow",length = 50)
    private String leEyebrow;
    @Column(name = "le_eyelid",length = 50)
    private String leEyelid;
    @Column(name = "le_cornea",length = 50)
    private String leCornea;
    @Column(name = "le_conjunctiva",length = 50)
    private String leConjunctiva;
    @Column(name = "le_fornix",length = 50)
    private String leFornix;
    @Column(name = "le_limbus",length = 50)
    private String leLimbus;
    @Column(name = "le_sclera",length = 50)
    private String leSclera;
    @Column(name = "le_anterior_chamber",length = 50)
    private String leAnteriorChamber;
    @Column(name = "le_iris",length = 50)
    private String leIris;
    @Column(name = "le_pupil",length = 50)
    private String lePupil;
    @Column(name = "le_lens",length = 50)
    private String leLens;
    @Column(name = "le_vitreous_anterior",length = 50)
    private String leVitreousAnterior;

    // -------- Fundus Details --------
    @Column(name = "re_optic_disc",length = 50)
    private String reOpticDisc;
    @Column(name = "re_fovea_macula",length = 50)
    private String reFoveaMacula;
    @Column(name = "re_vitreous_posterior",length = 50)
    private String reVitreousPosterior;
    @Column(name = "re_blood_vessels",length = 50)
    private String reBloodVessels;
    @Column(name = "re_retina",length = 50)
    private String reRetina;

    @Column(name = "le_optic_disc",length = 50)
    private String leOpticDisc;

    @Column(name = "le_fovea_macula",length = 50)
    private String leFoveaMacula;

    @Column(name = "le_vitreous_posterior",length = 50)
    private String leVitreousPosterior;

    @Column(name = "le_blood_vessels",length = 50)
    private String leBloodVessels;

    @Column(name = "le_retina",length = 50)
    private String leRetina;

    // -------- Audit --------
    @Column(name = "status", nullable = false,length =1)
    private String status ;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by",length = 200)
    private String createdBy;

    @Column(name = "last_updated_by",length = 200)
    private String lastUpdatedBy;
}