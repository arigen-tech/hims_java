package com.hims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Getter
@Setter
@Entity
@Table(name = "mas_crossmatch_type")
public class MasCrossMatchType {


        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "crossmatch_type_id")
        private Long id;

        @NotBlank(message = "Cross match code is required")
        @Size(max = 20, message = "Crossmatch code must not exceed 20 characters")
        @Column(name = "crossmatch_code", length = 20, nullable = false)
        private String crossMatchCode;

        @NotBlank(message = "Cross match name is required")
        @Size(max = 100, message = "Cross match name must not exceed 100 characters")
        @Column(name = "crossmatch_name", length = 100, nullable = false)
        private String crossMatchName;

        @Size(max = 300, message = "Description must not exceed 300 characters")
        @Column(name = "description", length = 300)
        private String description;

        @NotNull(message = "Turnaround time is required")
        @Column(name = "turnaround_time_min", nullable = false)
        private Integer turnaroundTimeMin;

        @NotNull(message = "Charge amount is required")
        @Column(name = "charge_amount", precision = 10, scale = 2, nullable = false)
        private BigDecimal chargeAmount;

        @NotBlank(message = "Emergency allowed flag is required")
        @Column(name = "is_emergency_allowed", length = 1, nullable = false)
        private String isEmergencyAllowed;

        @NotBlank(message = "Status is required")
        @Column(name = "status", length = 1)
        private String status;

        @Column(name = "created_date")
        private LocalDateTime createdDate;

        @Size(max = 200, message = "Created by must not exceed 200 characters")
        @Column(name = "created_by", length = 200)
        private String createdBy;
    }

