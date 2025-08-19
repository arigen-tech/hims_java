package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Data
@Table(name = "dg_sample_collection_header")
public class DgSampleCollectionHeader {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sample_collection_header_id")
    private Long sampleCollectionHeaderId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id")
    private  Patient patient_id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hospital_id")
    private MasHospital hospitalId;

   @ManyToOne(fetch = FetchType.EAGER)
   @JoinColumn(name = "department_id")
   private MasDepartment departmentId;

    @Column(name = "order_by_department")
    private Integer orderByDepartment;
    @Column(name = "collection_center_id")
    private Long collectionCenterId;

    @Column(name = "collection_center_modified_id")
    private Long collectionCenterModifiedId;

    @Column(name = "inpatient_id")
    private Long inpatientId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "visit_id")
    private Visit visitId;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sub_charge_code_id")
    private MasSubChargeCode subChargeCode;

   @Column(name = "priority")
    private String priority;

    @Column(name = "collection_by")
    private String collection_by;

    @Column(name = "collection_time")
    private LocalTime collection_time;


    @Column(name = "validation_date")
    private LocalDate validation_date;

    @Column(name = "validated_by")
    private String validatedBy;

    @Column(name = "sample_order_status")
    private String sample_order_status;

    @Column(name = "result_entry_status")
    private String result_entry_status;

    @Column(name = "remarks")
    private String remarks;

    @Column(name = "last_chg_by")
    private String lastChgBy;

    @Column(name = "last_chg_time")
    private LocalTime lastChgTime;

    @Column(name = "last_chg_date")
    private LocalDate lastChgDate;
}
