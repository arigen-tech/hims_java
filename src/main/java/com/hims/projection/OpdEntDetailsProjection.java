package com.hims.projection;
import java.time.LocalDate;

public interface OpdEntDetailsProjection {

    Long getEntId();

    Long getPatientId();
    Long getVisitId();
    LocalDate getOpdDate();

    String getRightPinna();
    String getLeftPinna();
    String getRightEarCanal();
    String getLeftEarCanal();
    String getRightTmStatus();
    String getLeftTmStatus();

    String getRinneTest();
    String getWeberTest();
    String getAbcTest();
    String getAudiometryFindings();

    String getExternalNose();
    String getNasalMucosa();
    String getSeptum();
    String getTurbinates();
    String getNasalPolyp();
    String getNasalDischarge();
    String getMaxillaryTenderness();
    String getFrontalTenderness();

    String getOralCavity();
    String getTonsilGrade();
    String getTonsilCongestion();
    String getTonsilFollicles();
    String getTonsilMembrane();
    String getPeritonsillarAbscess();

    String getPharynx();
    String getUvula();
    String getVoiceQuality();

    String getThyroidEnlargement();
    String getCervicalNodes();
    String getNeckMass();
    String getNeckOtherFindings();
}