package com.hims.entity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_nursing_type")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasNursingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nursing_type_id")
    private Long nursingTypeId;

    @Column(name = "nursing_type_name", nullable = false, length = 100)
    private String nursingTypeName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", length = 1, nullable = false)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
