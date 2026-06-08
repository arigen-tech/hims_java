package com.hims.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO for OPD OBG (Obstetrics and Gynecology) Details
 * Maps database records to API response format
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpdObgDetailsResponse {

    private Long obgId;
    private Long patientId;
    private Long visitId;
    private LocalDate opdDate;

    // Obstetric History
    private String obstetricHistoryNotes;
    private String gravida;
    private String para;
    private String abortions;
    private String livingChildren;

    // Basic OBG Info
    private String conceptionType;
    private String marriedLifeYears;
    private String consanguinity;
    private String bookedStatus;
    private String immunisedStatus;

    // Trimester & General Findings
    private String trimester;
    private String gc; // Gestational Calculation
    private String pallor;
    private String pedalEdema;

    // Vitals & Respiratory
    private String respiratorySystem;
    private String breathSounds;

    // Cardiovascular
    private String cardiovascularS1;
    private String cardiovascularS2;
    private String cardiovascularMurmurs;

    // Tetanus & Fetal
    private String ttStatus;
    private String fhr; // Fetal Heart Rate
    private String presentation;
    private String palpationNotes;

    // Per Vaginal Examination
    private String pvDone;
    private String uterusHeight;
    private String uterusHeightSpecify;
    private String antenatalRemarks;

    // Menstrual History
    private String menarcheAge;
    private String cycles;
    private String rangeDays;
    private String intervalDays;
    private String menstrualFlow;
    private String menstrualPause;

    // PV Examination Details
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

    // Gynecology Fields
    private String gynFlow;
    private String gynMenarcheAge;
    private LocalDate gynLastMenstrualPeriod;
    private String gynMenstrualPattern;
    private String gynCycleType;
    private String sterilisation;

    // Abdominal & Local Examination
    private String abdomenInspection;
    private String abdomenPalpation;
    private String papSmearResult;
    private String localExaminationNotes;
    private String perSpeculum;
    private String bimanualExamination;

    // System Fields
    private String status;
    private LocalDateTime lastUpdateDate;
    private String createdBy;
    private String lastUpdatedBy;
}

