package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_intake_type")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasIntakeType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "intake_type_id")
    private Long intakeTypeId;

    @Column(name = "intake_type_name", nullable = false, length = 50, unique = true)
    private String intakeTypeName;

    @Column(name = "is_liquid", nullable = false, length = 1)
    private String isLiquid;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
