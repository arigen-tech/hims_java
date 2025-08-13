package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "dg_sample_collection_details")

public class DgSampleCollectionDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sample_collection_details_id")
    private Long sampleCollectionDetailsId;

    @Column(name = "collected", length = 1)
    private String collected;

    @Column(name = "order_status", length = 1)
    private String orderStatus;

    @Column(name = "remarks", length = 100)
    private String remarks;

    @Column(name = "last_chg_by", length = 12)
    private String lastChgBy;

    @Column(name = "last_chg_time", length = 10)
    private String lastChgTime;

    @Column(name = "last_chg_date")
    private LocalDate lastChgDate;

    @Column(name = "sample_no", length = 30)
    private String sampleNo;

    @Column(name = "validated", length = 1)
    private String validated;

    @Column(name = "reason", length = 45)
    private String reason;

    @Column(name = "diag_no", length = 30)
    private String diagNo;

    @Column(name = "sample_coll_datetime")
    private LocalDate sampleCollDatetime;

    @Column(name = "quantity", length = 10)
    private String quantity;

    @Column(name = "rejected", length = 1)
    private String rejected;

    @Column(name = "subcharge")
    private Integer subcharge;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sample_collection_header_id")
    private DgSampleCollectionHeader sampleCollectionHeaderId;

    @Column(name = "maincharge")
    private Integer maincharge;

    @Column(name = "charge_code_id")
    private  Long chargeCodeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sample_id")
    private DgMasSample sampleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "investigation_id")
    private DgMasInvestigation investigationId;

    @Column(name = "collected_by")
    private Integer collectedBy;

    @Column(name = "orderdt_id")
    private Integer orderdtId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "main_chargecode_id")
    private MasMainChargeCode mainChargecodeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_chargecode_id")
    private MasSubChargeCode subChargecodeId;

    @Column(name = "collection_id")
    private Long collectionId;

    @Column(name = "collection_center_id")
    private Long collectionCenterId;

    @Column(name = "empanelled_status", length = 1)
    private String empanelledStatus;
}
