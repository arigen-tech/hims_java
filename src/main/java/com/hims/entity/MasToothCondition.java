package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_tooth_condition")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasToothCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "condition_id")
    private Long conditionId;

    @Column(name = "condition_name", length = 50, nullable = false)
    private String conditionName;

    @Column(name = "is_exclusive", length = 1)
    private String isExclusive;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "points")
    private Integer points;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;
}
