package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@Table(name = "dg_sample_collection_details")
public class DgSampleCollectionDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sample_collection_details_id")
    private Long sampleCollectionDetailsId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sample_collection_header_id")
    private DgSampleCollectionHeader sampleCollectionHeader;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "investigation_id")
    private DgMasInvestigation investigationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sample_id")
    private DgMasSample sampleId;

    @Column(name = "sample_order_status", length = 1)
    private String orderStatus;

    @Column(name = "collection_time")
    private LocalDateTime sampleCollDatetime;

    @Column(name = "collected_by")
    private Integer collectedBy;

    @Column(name = "quantity", length = 10)
    private BigDecimal quantity;


    @Column(name = "rejected_reason", length = 200)
    private String rejected_reason;

    @Column(name = "validated", length = 1)
    private String validated;

    @Column(name = "result_status", length = 1)
    private String result_status;

    @Column(name = "remarks", length = 500)
    private String remarks;

    @Column(name = "empanelled_status", length = 1)
    private String empanelledStatus;
    @Column(name="old_sample_collection_hd_id_for_reject")
    private Long  oldSampleCollectionHdIdForReject ;

    @Column(name = "sample_generated_id",length = 100)
    private String sampleGeneratedId;
}
