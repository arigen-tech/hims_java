package com.hims.projection;
import java.time.LocalDate;

public interface OphthalmologyExaminationDetailProjection {

    Long getPatientId();
    Long getVisitId();
    LocalDate getOpdDate();

    // -------- Distance Vision --------
    String getReDistanceUnaided();
    String getReDistancePinhole();
    String getReDistanceBestCorrected();
    String getLeDistanceUnaided();
    String getLeDistancePinhole();
    String getLeDistanceBestCorrected();

    // -------- Near Vision --------
    String getReNearUnaided();
    String getReNearPinhole();
    String getReNearBestCorrected();
    String getLeNearUnaided();
    String getLeNearPinhole();
    String getLeNearBestCorrected();

    // -------- Fundus --------
    String getFundusGlow();

    // -------- Retinoscopy --------
    String getReRetinoscopyAxis();
    String getReRetinoscopyV();
    String getReRetinoscopyH();
    String getLeRetinoscopyAxis();
    String getLeRetinoscopyV();
    String getLeRetinoscopyH();

    // -------- Measurements --------
    String getReKeratometry();
    String getRePachymetry();
    String getReTonometry();
    String getReFieldOfVision();
    String getReIolPower();

    String getLeKeratometry();
    String getLePachymetry();
    String getLeTonometry();
    String getLeFieldOfVision();
    String getLeIolPower();

    // -------- Refraction Distance --------
    String getReSphDist();
    String getReCylDist();
    String getReAxisDist();
    String getLeSphDist();
    String getLeCylDist();
    String getLeAxisDist();

    // -------- Refraction Near --------
    String getReSphNear();
    String getReCylNear();
    String getReAxisNear();
    String getLeSphNear();
    String getLeCylNear();
    String getLeAxisNear();

    // -------- General --------
    String getIpdValue();
    String getSpectacleUse();
    String getLensType();

    // -------- External Eye (RE) --------
    String getReEyebrow();
    String getReEyelid();
    String getReCornea();
    String getReConjunctiva();
    String getReFornix();
    String getReLimbus();
    String getReSclera();
    String getReAnteriorChamber();
    String getReIris();
    String getRePupil();

    // -------- External Eye (LE) --------
    String getLeEyebrow();
    String getLeEyelid();
    String getLeCornea();
    String getLeConjunctiva();
    String getLeFornix();
    String getLeLimbus();
    String getLeSclera();
    String getLeAnteriorChamber();
    String getLeIris();
    String getLePupil();

    // -------- Fundus Details --------
    String getReOpticDisc();
    String getReFoveaMacula();
    String getReVitreousPosterior();
    String getReBloodVessels();
    String getReRetina();

    String getLeOpticDisc();
    String getLeFoveaMacula();
    String getLeVitreousPosterior();
    String getLeBloodVessels();
    String getLeRetina();
    String getLeColourVision();
     String getReColourVision();
}