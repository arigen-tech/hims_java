package com.hims.entity;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "ipd_consultation_tariff", schema = "public")
public class IpdConsultationTariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tariff_id")
    private Long tariffId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_category_id")
    private MasServiceCategory serviceCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_type_id")
    private MasVisitType visitType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospital;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private MasDepartment department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @Column(name = "base_tariff", precision = 12, scale = 2)
    private BigDecimal baseTariff;

    @Column(name = "from_dt")
    private LocalDateTime fromDate;

    @Column(name = "to_dt")
    private LocalDateTime toDate;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_chg_by", length = 300)
    private String lastChangedBy;

    @Column(name = "last_chg_dt")
    private LocalDateTime lastChangedDate;

}