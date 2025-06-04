package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mas_service_opd")
public class MasServiceOpd {
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 20)
    @NotNull
    @Column(name = "service_code", nullable = false, length = 20)
    private String serviceCode;

    @Size(max = 400)
    @NotNull
    @Column(name = "service_name", nullable = false, length = 400)
    private String serviceName;

    @NotNull
    @Column(name = "base_tariff", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseTariff;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "service_cat_id", nullable = false)
    private MasServiceCategory serviceCat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospital;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private MasDepartment department;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @Size(max = 1)
    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "from_dt")
    private Instant fromDt;

    @Column(name = "to_dt")
    private Instant toDt;

}
