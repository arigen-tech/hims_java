package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_output_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasOutputType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "output_type_id")
    private Long outputTypeId;

    @Column(name = "output_type_name", length = 50, nullable = false)
    private String outputTypeName;

    @Column(name = "is_measurable", length = 1)
    private String isMeasurable;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @Column(name = "description", length = 200)
    private String description;
}
