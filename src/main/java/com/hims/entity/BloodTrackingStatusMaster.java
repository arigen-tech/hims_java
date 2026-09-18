package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "blood_tracking_status_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BloodTrackingStatusMaster {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "status_id")
    private Long statusId;

    @Column(name = "status_code", nullable = false, length = 50)
    private String statusCode;

    @Column(name = "status_name", nullable = false, length = 100)
    private String statusName;

    @Column(name = "status_description", length = 255)
    private String statusDescription;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "is_final", nullable = false)
    private Boolean isFinal = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
