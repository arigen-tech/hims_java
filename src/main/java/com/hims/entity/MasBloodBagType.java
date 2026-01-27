package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_blood_bag_type")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasBloodBagType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bag_type_id")
    private Long bagTypeId;

    @Column(name = "bag_type_code")
    private String bagTypeCode;

    @Column(name = "bag_type_name")
    private String bagTypeName;

    @Column(name = "description")
    private String description;

    @Column(name = "max_components")
    private Integer maxComponents;

    @Column(name = "status")
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "last_updated_by")
    private String lastUpdatedBy;
}
