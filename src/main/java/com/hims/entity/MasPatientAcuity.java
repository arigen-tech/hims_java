package com.hims.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_patient_acuity")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasPatientAcuity {
    @Id
    @Column(name = "acuity_code", length = 10,unique = true)
    private String acuityCode;

    @Column(name = "acuity_name", length = 100)
    private String acuityName;

    @Column(columnDefinition = "text")
    private String description;

    @Column(length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
