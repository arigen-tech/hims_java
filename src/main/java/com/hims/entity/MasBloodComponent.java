package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_blood_component", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasBloodComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "component_id")
    private Long componentId;

    @Column(name = "component_code", length = 20)
    private String componentCode;

    @Column(name = "component_name", length = 50)
    private String componentName;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "storage_temp", length = 20)
    private String storageTemp;

    @Column(name = "shelf_life_days")
    private Integer shelfLifeDays;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;



}
