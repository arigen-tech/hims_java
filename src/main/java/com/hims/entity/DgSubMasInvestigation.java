package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "dg_sub_mas_investigation")
public class DgSubMasInvestigation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sub_investigation_id", nullable = false)
    private Long subInvestigationId;

    @Column(name = "sub_investigation_code", length = 8)
    private String subInvestigationCode;

    @Column(name = "sub_investigation_name")
    private String subInvestigationName;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "order_no")
    private Long orderNo;

    @Column(name = "last_chg_by", length = 12)
    private String lastChgBy;

    @Column(name = "last_chg_date")
    private Instant lastChgDate;

    @Column(name = "last_chg_time", length = 10)
    private String lastChgTime;

    @Column(name = "result_type", length = 1)
    private String resultType;

    @Column(name = "comparison_type", length = 1)
    private String comparisonType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "main_chargecode_id")
    private MasMainChargeCode mainChargeCodeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_chargecode_id")
    private MasSubChargeCode subChargeCodeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sample_id")
    private DgMasSample sampleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "uomId")
    private DgUom uomId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "investigation_id")
    private DgMasInvestigation investigationId;

    @Column(name = "fixed_value_expected_result", length = 1)
    private String fixedValueExpectedValue;

}
