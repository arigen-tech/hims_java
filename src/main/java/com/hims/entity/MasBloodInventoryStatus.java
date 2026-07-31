package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_blood_inventory_status")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasBloodInventoryStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_status_id")
    private Long inventoryStatusId;

    @Column(name = "status_code", length = 20, nullable = false, unique = true)
    private String statusCode;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
