package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_component_failure_reason")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasComponentFailureReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "failure_reason_id")
    private Long failureReasonId;

    @Column(name = "failure_reason_code", length = 50, nullable = false, unique = true)
    private String failureReasonCode;

    @Column(name = "failure_reason_name", length = 100, nullable = false)
    private String failureReasonName;

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