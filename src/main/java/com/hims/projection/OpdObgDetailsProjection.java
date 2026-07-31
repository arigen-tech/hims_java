package com.hims.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface OpdObgDetailsProjection {

    Long getObgId();
    Long getPatientId();
    Long getVisitId();
    LocalDate getOpdDate();

    String getObstetricHistoryNotes();
    String getGravida();
    String getPara();
    String getAbortions();
    String getLivingChildren();

    String getConceptionType();
    String getMarriedLifeYears();
    String getConsanguinity();
    String getBookedStatus();
    String getImmunisedStatus();

    String getTrimester();
    String getGc();
    String getPallor();
    String getPedalEdema();

    String getRespiratorySystem();
    String getBreathSounds();

    String getCardiovascularS1();
    String getCardiovascularS2();
    String getCardiovascularMurmurs();

    String getTtStatus();
    String getFhr();
    String getPresentation();
    String getPalpationNotes();

    String getPvDone();
    String getUterusHeight();
    String getUterusHeightSpecify();
    String getAntenatalRemarks();

    String getMenarcheAge();
    String getCycles();
    String getRangeDays();
    String getIntervalDays();
    String getMenstrualFlow();
    String getMenstrualPause();

    String getPvOsDilatation();
    String getPvEffacement();
    String getPvMembrane();
    String getPvLiquor();

    String getCervixConsistency();
    String getCervixPosition();
    String getCervixLength();

    String getStationPresenting();
    String getFetalHead();
    String getPelvis();

    String getGynFlow();
    String getGynMenarcheAge();
    LocalDate getGynLastMenstrualPeriod();

    String getGynMenstrualPattern();
    String getGynCycleType();
    String getSterilisation();

    String getAbdomenInspection();
    String getAbdomenPalpation();

    String getPapSmearResult();
    String getLocalExaminationNotes();
    String getPerSpeculum();
    String getBimanualExamination();

    String getStatus();
    LocalDateTime getLastUpdateDate();
    String getCreatedBy();
    String getLastUpdatedBy();
    String getGynObstetricHistory();
}