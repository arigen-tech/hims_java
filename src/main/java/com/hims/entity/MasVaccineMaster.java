package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_vaccine_master")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasVaccineMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vaccine_id")
    private Long vaccineId;

    @Column(name = "vaccine_label", nullable = false, length = 200)
    private String vaccineLabel;

    @Column(name = "recommended_age", nullable = false, length = 50)
    private String recommendedAge;

    @Column(name = "vaccine_group", nullable = false, length = 50)
    private String vaccineGroup;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_multi_dose", length = 1)
    private String isMultiDose;   // Y / N

    @Column(name = "dose_per_vial")
    private Integer dosePerVial;

    @Column(length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
