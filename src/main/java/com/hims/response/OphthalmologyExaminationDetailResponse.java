package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
@Data
public class OphthalmologyExaminationDetailResponse {
    private Long patientId;
    private Long visitId;
    private LocalDate opdDate;

    // -------- Distance Vision --------
    private String reDistanceUnaided;
    private String reDistancePinhole;
    private String reDistanceBestCorrected;
    private String leDistanceUnaided;
    private String leDistancePinhole;
    private String leDistanceBestCorrected;

    // -------- Near Vision --------
    private String reNearUnaided;
    private String reNearPinhole;
    private String reNearBestCorrected;
    private String leNearUnaided;
    private String leNearPinhole;
    private String leNearBestCorrected;

    // -------- Fundus --------
    private String fundusGlow;


    // -------- Retinoscopy --------
    private String reRetinoscopyAxis;
    private String reRetinoscopyV;
    private String reRetinoscopyH;
    private String leRetinoscopyAxis;
    private String leRetinoscopyV;
    private String leRetinoscopyH;

    // -------- Measurements --------
    private String reKeratometry;
    private String rePachymetry;
    private String reTonometry;
    private String reFieldOfVision;
    private String reIolPower;

    private String leKeratometry;
    private String lePachymetry;
    private String leTonometry;
    private String leFieldOfVision;
    private String leIolPower;

    // -------- Refraction Distance --------
    private String reSphDist;
    private String reCylDist;
    private String reAxisDist;
    private String leSphDist;
    private String leCylDist;
    private String leAxisDist;

    // -------- Refraction Near --------
    private String reSphNear;
    private String reCylNear;
    private String reAxisNear;
    private String leSphNear;
    private String leCylNear;
    private String leAxisNear;

    // -------- General --------
    private String ipdValue;
    private String spectacleUse;
    private String lensType;

    // -------- External Eye (RE) --------
    private String reEyebrow;
    private String reEyelid;
    private String reCornea;
    private String reConjunctiva;
    private String reFornix;
    private String reLimbus;
    private String reSclera;
    private String reAnteriorChamber;
    private String reIris;
    private String rePupil;


    // -------- External Eye (LE) --------
    private String leEyebrow;
    private String leEyelid;
    private String leCornea;
    private String leConjunctiva;
    private String leFornix;
    private String leLimbus;
    private String leSclera;
    private String leAnteriorChamber;
    private String leIris;
    private String lePupil;


    // -------- Fundus Details --------
    private String reOpticDisc;
    private String reFoveaMacula;
    private String reVitreousPosterior;
    private String reBloodVessels;
    private String reRetina;

    private String leOpticDisc;
    private String leFoveaMacula;
    private String leVitreousPosterior;
    private String leBloodVessels;
    private String leRetina;
}
