package com.hims.request;


import lombok.Data;

import java.time.LocalDate;

@Data
public class OpdObgDetailsRequest {
    private Long patientId;
    private Long visitId;
    private LocalDate opdDate;
    private String obstetricHistoryNotes;
    private String gravida;
    private String para;
    private String abortions;
    private String livingChildren;
    private String conceptionType;
    private String marriedLifeYears;
    private String consanguinity;
    private String bookedStatus;
    private String immunisedStatus;
    private String trimester;
    private String gc;
    private String pallor;
    private String pedalEdema;
    private String respiratorySystem;
    private String breathSounds;
    private String cardiovascularS1;
    private String cardiovascularS2;
    private String cardiovascularMurmurs;
    private String ttStatus;
    private String fhr;
    private String presentation;
    private String palpationNotes;
    private String pvDone;
    private String uterusHeight;
    private String uterusHeightSpecify;
    private String antenatalRemarks;
    private String menarcheAge;
    private String cycles;
    private String rangeDays;
    private String intervalDays;
    private String menstrualFlow;
    private String menstrualPause;
    private String pvOsDilatation;
    private String pvEffacement;
    private String pvMembrane;
    private String pvLiquor;
    private String cervixConsistency;
    private String cervixPosition;
    private String cervixLength;
    private String stationPresenting;
    private String fetalHead;
    private String pelvis;
    private String gynFlow;
    private String gynMenarcheAge;
    private LocalDate gynLastMenstrualPeriod;
    private String gynMenstrualPattern;
    private String gynCycleType;
    private String sterilisation;
    private String abdomenInspection;
    private String abdomenPalpation;
    private String papSmearResult;
    private String localExaminationNotes;
    private String perSpeculum;
    private String bimanualExamination;


}