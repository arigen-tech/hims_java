package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "visit")
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "visit_id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "token_no", nullable = false)
    private Long tokenNo;

    @Column(name = "visit_date")
    private Instant visitDate;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Size(max = 1)
    @NotNull
    @Column(name = "visit_status", nullable = false, length = 1)
    private String visitStatus;

    @Column(name = "priority")
    private Long priority;

    @Column(name = "department_id")
    private Long departmentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @Size(max = 100)
    @Column(name = "doctor_name", length = 100)
    private String doctorName;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private MasHospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ini_doctor_id")
    private User iniDoctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private MasOpdSession session;

    @Size(max = 1)
    @NotNull
    @Column(name = "billing_status", nullable = false, length = 1)
    private String billingStatus;

}
