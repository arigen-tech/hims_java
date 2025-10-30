package com.hims.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "dg_result_entry_header")
public class DgResultEntryHeader {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "result_entry_id")
    private Long resultEntryId;

    @Column(name = "result_no",length = 30)
    private String resultNo;

    @Column(name = "result_date")
    private LocalDate resultDate;

    @Column(name = "result_time",length=20)
    private String resultTime;

    @Column(name = "verified_on")
    private  LocalDate verifiedOn;

    @Column(name = "verified_time",length = 10)
    private String verifiedTime;

    @Column(name = "verified", length = 1)
    private String verified;

    @Column(name = "result_status",length = 1)
    private String resultStatus;

    @Column(name = "remarks",length = 100)
    private String remarks;

    @Column(name = "template_id",length = 20)
    private String templateId;

    @Column(name = "last_chgd_by",length = 20)
    private String lastChgdBy;

    @Column(name = "last_chgd_date")
    @UpdateTimestamp
    private LocalDate lastChgdDate;

    @Column(name = "last_chgd_time",length = 20)
    private  String lastChgdTime;

    @Column(name = "received_by",length = 50)
    private String receivedBy;

    @Column(name = "result_type",length = 1)
    private String resultType;

    @Column(name = "impression",length = 50)
    private String impression;

    @Column(name = "test_order_no")
    private Integer testOrderNo;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospitalId;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "investigation_id")
//    private DgMasInvestigation investigationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "main_chargecode_id")
    private  MasMainChargeCode mainChargecodeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private MasDepartment departmentId;


   // private Integer prescribedBy;//

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sample_collection_header_id")
    private DgSampleCollectionHeader sampleCollectionHeaderId;


   // private Integer inpatientId;//

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "relation_id")
    private MasRelation relationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_chargecode_id")
    private MasSubChargeCode subChargeCodeId;


  //  private Integer resultVerifiedBy;//


 //   private Integer hinId;//

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private MasEmployee employeeId;


 //   private Integer resultUpdatedBy;//

    @UpdateTimestamp
    private LocalDateTime updateOn;



}
