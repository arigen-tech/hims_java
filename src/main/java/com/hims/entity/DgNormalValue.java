package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "dg_normal_value")
public class DgNormalValue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "normal_id", nullable = false)
    private Long normalId;

    @Column(name = "sex", length = 1)
    private String sex;

    @Column(name = "from_age")
    private Long fromAge;

    @Column(name = "to_age")
    private Long toAge;

    @Column(name = "min_normal_value", length = 10)
    private String minNormalValue;

    @Column(name = "max_normal_value", length = 10)
    private String maxNormalValue;

    @Column(name = "normalvalue")
    private String normalValue;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_investigation_id")
    private DgSubMasInvestigation subInvestigationId ;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "charge_code_id")
    private MasMainChargeCode mainChargeCodeId;
}
