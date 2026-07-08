package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "mas_patient_condition")
public class MasPatientCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "patient_condition_id", nullable = false)
    private Long patientConditionId;

    @Size(max = 50)
    @Column(name = "patient_condition_name", length = 50)
    private String patientConditionName;

    @Size(max = 200)
    @Column(name = "description", length = 200)
    private String description;

    @Size(max = 1)
    @Column(name = "status", length = 1)
    private String status = "Y";

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Size(max = 200)
    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Size(max = 200)
    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;


}