package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "rad_orderhd")
public class RadOrderHd {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rad_orderhd_id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "appointment_date")
    private LocalDate appointmentDate ;

    @Column(name = "order_time")
    private Instant orderTime;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id")
    private Visit visit;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "hospital_id", nullable = false)
    private MasHospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private MasDepartment department;

    @Size(max = 200)
    @Column(name = "prescribed_by", length = 200)
    private String prescribedBy;

    @Size(max = 200)
    @Column(name = "createdby", length = 200)
    private String createdby;

    @Column(name = "createdon")
    private Instant createdon;

    @Size(max = 200)
    @Column(name = "last_chg_by", length = 200)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @OneToMany(mappedBy = "radOrderhd")
    private Set<RadOrderDt> radOrderDts = new LinkedHashSet<>();

    @Size(max = 1)
    @Column(name = "payment_status", length = 1)
    private String paymentStatus;

    @OneToMany(mappedBy = "radOrderHd")
    private Set<BillingHeader> billingHeaders = new LinkedHashSet<>();

}
