package com.hims.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "opd_obg_details")
public class OpdObgDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "obg_id")
    private Long obgId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @Column(name = "opd_date")
    private LocalDate opdDate;

    @Column(name = "obstetric_history_notes", length = 500)
    private String obstetricHistoryNotes;

    @Column(name = "gravida", length = 10)
    private String gravida;

    @Column(name = "para", length = 10)
    private String para;

    @Column(name = "abortions", length = 10)
    private String abortions;

    @Column(name = "living_children", length = 10)
    private String livingChildren;

    @Column(name = "conception_type", length = 100)
    private String conceptionType;

    @Column(name = "married_life_years", length = 10)
    private String marriedLifeYears;

    @Column(name = "consanguinity", length = 50)
    private String consanguinity;

    @Column(name = "booked_status", length = 50)
    private String bookedStatus;

    @Column(name = "immunised_status", length = 50)
    private String immunisedStatus;

    @Column(name = "trimester", length = 50)
    private String trimester;

    @Column(name = "gc", length = 50)
    private String gc;

    @Column(name = "pallor", length = 50)
    private String pallor;

    @Column(name = "pedal_edema", length = 50)
    private String pedalEdema;

    @Column(name = "respiratory_system", length = 100)
    private String respiratorySystem;

    @Column(name = "breath_sounds", length = 100)
    private String breathSounds;

    @Column(name = "cardiovascular_s1", length = 50)
    private String cardiovascularS1;

    @Column(name = "cardiovascular_s2", length = 50)
    private String cardiovascularS2;

    @Column(name = "cardiovascular_murmurs", length = 100)
    private String cardiovascularMurmurs;

    @Column(name = "tt_status", length = 50)
    private String ttStatus;

    @Column(name = "fhr", length = 20)
    private String fhr;

    @Column(name = "presentation", length = 50)
    private String presentation;

    @Column(name = "palpation_notes", length = 500)
    private String palpationNotes;

    @Column(name = "pv_done", length = 20)
    private String pvDone;

    @Column(name = "uterus_height", length = 50)
    private String uterusHeight;

    @Column(name = "uterus_height_specify", length = 200)
    private String uterusHeightSpecify;

    @Column(name = "antenatal_remarks", length = 500)
    private String antenatalRemarks;

    @Column(name = "menarche_age", length = 10)
    private String menarcheAge;

    @Column(name = "cycles", length = 50)
    private String cycles;

    @Column(name = "range_days", length = 20)
    private String rangeDays;

    @Column(name = "interval_days", length = 20)
    private String intervalDays;

    @Column(name = "menstrual_flow", length = 50)
    private String menstrualFlow;

    @Column(name = "menstrual_pause", length = 100)
    private String menstrualPause;

    @Column(name = "pv_os_dilatation", length = 20)
    private String pvOsDilatation;

    @Column(name = "pv_effacement", length = 20)
    private String pvEffacement;

    @Column(name = "pv_membrane", length = 50)
    private String pvMembrane;

    @Column(name = "pv_liquor", length = 50)
    private String pvLiquor;

    @Column(name = "cervix_consistency", length = 50)
    private String cervixConsistency;

    @Column(name = "cervix_position", length = 50)
    private String cervixPosition;

    @Column(name = "cervix_length", length = 50)
    private String cervixLength;

    @Column(name = "station_presenting", length = 10)
    private String stationPresenting;

    @Column(name = "fetal_head", length = 50)
    private String fetalHead;

    @Column(name = "pelvis", length = 50)
    private String pelvis;

    @Column(name = "gyn_flow", length = 50)
    private String gynFlow;

    @Column(name = "gyn_menarche_age", length = 10)
    private String gynMenarcheAge;

    @Column(name = "gyn_last_menstrual_period")
    private LocalDate gynLastMenstrualPeriod;

    @Column(name = "gyn_menstrual_pattern", length = 50)
    private String gynMenstrualPattern;

    @Column(name = "gyn_cycle_type", length = 50)
    private String gynCycleType;

    @Column(name = "sterilisation", length = 50)
    private String sterilisation;

    @Column(name = "abdomen_inspection", length = 500)
    private String abdomenInspection;

    @Column(name = "abdomen_palpation", length = 500)
    private String abdomenPalpation;

    @Column(name = "pap_smear_result", length = 100)
    private String papSmearResult;

    @Column(name = "local_examination_notes", length = 500)
    private String localExaminationNotes;

    @Column(name = "per_speculum", length = 500)
    private String perSpeculum;

    @Column(name = "bimanual_examination", length = 500)
    private String bimanualExamination;

    @Column(name = "gyn_obstetric_history", length = 500)
    private String gynObstetricHistory;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}