package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_drug_schedule", schema = "public")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasDrugSchedule {

    @Id
    @Column(name = "schedule_code", length = 5)
    private String scheduleCode;

    @Column(name = "schedule_name", length = 50)
    private String scheduleName;

    @Column(name = "legal_description", length = 300)
    private String legalDescription;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "last_updated_by", length = 100)
    private String lastUpdatedBy;
}