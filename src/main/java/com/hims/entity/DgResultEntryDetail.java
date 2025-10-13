//package com.hims.entity;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//@Getter
//@Setter
//@AllArgsConstructor
//@NoArgsConstructor
//@Entity
//@Table(name = "dg_result_entry_detail")
//public class DgResultEntryDetail {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "result_entry_detail_id")
//    private Long resultEntryDetailId;
//
//    @Column(name = "result_type",length = 1)
//    private String resultType;
//
//    @Column(name = "result",columnDefinition = "text")
//    private String result;
//
//    @Column(name = "remarks",length = 200)
//    private String remarks;
//
//    @Column(name = "film_size",length = 15)
//    private String filmSize;
//
//    @Column(name = "film_used")
//    private Integer filmUsed;//
//
//    @Column(name = "validated",length = 1)
//    private String validated;
//
//    @Column(name = "result_detail_status",length = 1)
//    private String resultDetailStatus;
//
//    @Column(name = "hl7_flag",length = 15)
//    private String hl7Flag;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "result_entry_id")
//    private DgResultEntryHeader resultEntryId;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "charge_code_id")
//    private MasMainChargeCode chargeCodeId;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "sample_id")
//    private DgMasSample sampleId;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "uom_id")
//    private MasStoreUnit uomId;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "investigation_id")
//    private DgMasInvestigation investigationId;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "normal_id")
//    private DgNormalValue normalId;
//
//    private Integer fixedId;//
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "template_id")
//    private MasTemplate templateId;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "sub_investigation_id")
//    private DgSubMasInvestigation subInvestigationId;
//
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "sample_collection_details_id")
//    private DgSampleCollectionDetails sampleCollectionDetailsId;
//
//    private Integer nprmalId;
//
//
//}
