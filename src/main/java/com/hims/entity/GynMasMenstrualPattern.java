package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "gyn_mas_menstrual_pattern", schema = "public")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GynMasMenstrualPattern {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "pattern_value", length = 50, nullable = false)
    private String patternValue;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
