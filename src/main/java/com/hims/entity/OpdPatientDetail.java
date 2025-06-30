package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "opd_patient_details")
public class OpdPatientDetail {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "opd_patient_details_id", nullable = false)
    private Long opdPatientDetailsId;

    @Size(max = 40)
    @Column(name = "height", length = 40)
    private String height;

    @Size(max = 40)
    @Column(name = "ideal_weight", length = 40)
    private String idealWeight;

    @Size(max = 40)
    @Column(name = "weight", length = 40)
    private String weight;

    @Size(max = 40)
    @Column(name = "pulse", length = 40)
    private String pulse;

    @Size(max = 48)
    @Column(name = "temperature", length = 48)
    private String temperature;

    @Column(name = "opd_date")
    private Instant opdDate;

    @Size(max = 12)
    @Column(name = "rr", length = 12)
    private String rr;

    @Column(name = "bmi", length = Integer.MAX_VALUE)
    private String bmi;

    @Size(max = 120)
    @Column(name = "spo2", length = 120)
    private String spo2;

    @Column(name = "varation")
    private Double varation;

    @Size(max = 3)
    @Column(name = "bp_systolic", length = 3)
    private String bpSystolic;

    @Size(max = 3)
    @Column(name = "bp_diastolic", length = 3)
    private String bpDiastolic;

    @Column(name = "icd_diag", length = Integer.MAX_VALUE)
    private String icdDiag;

    @Column(name = "working_diag", length = Integer.MAX_VALUE)
    private String workingDiag;

    @Column(name = "follow_up_flag", length = Integer.MAX_VALUE)
    private String followUpFlag;

    @Column(name = "follow_up_days")
    private Long followUpDays;

    @Column(name = "past_medical_history", length = Integer.MAX_VALUE)
    private String pastMedicalHistory;

    @Column(name = "present_complaints", length = Integer.MAX_VALUE)
    private String presentComplaints;

    @Column(name = "family_history", length = Integer.MAX_VALUE)
    private String familyHistory;

    @Column(name = "treatment_advice", length = Integer.MAX_VALUE)
    private String treatmentAdvice;

    @Column(name = "sos_flag", length = Integer.MAX_VALUE)
    private String sosFlag;

    @Size(max = 500)
    @Column(name = "recmmd_med_advice", length = 500)
    private String recmmdMedAdvice;

    @Size(max = 1)
    @Column(name = "medicine_flag", length = 1)
    private String medicineFlag;

    @Size(max = 1)
    @Column(name = "lab_flag", length = 1)
    private String labFlag;

    @Size(max = 1)
    @Column(name = "radio_flag", length = 1)
    private String radioFlag;

    @Size(max = 1)
    @Column(name = "referral_flag", length = 1)
    private String referralFlag;

    @Size(max = 1)
    @Column(name = "mlc_flag", length = 1)
    private String mlcFlag;

    @Size(max = 100)
    @Column(name = "police_station", length = 100)
    private String policeStation;

    @Size(max = 100)
    @Column(name = "police_name", length = 100)
    private String policeName;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "visit_id")
    private Visit visit;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "department_id")
    private MasDepartment department;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospital;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Size(max = 200)
    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

}
