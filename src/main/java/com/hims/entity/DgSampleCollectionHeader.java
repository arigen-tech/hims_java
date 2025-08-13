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

    @Column(name = "last_chg_by")
    private String lastChgBy;

    @Column(name = "last_chg_time")
    private LocalTime lastChgTime;

    @Column(name = "last_chg_date")
    private LocalDate lastChgDate;

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "diagnosis_date")
    private LocalDate diagnosisDate;

    @Column(name = "diagnosis_time")
    private LocalTime diagnosisTime;

    @Column(name = "sample_validation_date")
    private LocalDate sampleValidationDate;

    @Column(name = "sample_validation_time")
    private LocalTime sampleValidationTime;

    @Column(name = "patient_type")
    private String patientType;

    @Column(name = "collection_center_id")
    private Long collectionCenterId;

    @Column(name = "collection_center_modified_id")
    private Long collectionCenterModifiedId;

    @Column(name = "hospital_id")
    private Long hospitalId;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "order_by_department")
    private String orderByDepartment;

    @Column(name = "validated_by")
    private String validatedBy;

    @Column(name = "inpatient_id")
    private Long inpatientId;

    @Column(name = "visit_id")
    private Long visitId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "hin_id")
    private Long hinId;

    @Column(name = "orderhd_id")
    private Long orderHdId;

    @Column(name = "collection_time")
    private LocalTime collectionTime;
}
