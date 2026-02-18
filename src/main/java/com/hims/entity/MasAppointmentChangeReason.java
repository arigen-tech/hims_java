package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "mas_appointment_change_reason")
public class MasAppointmentChangeReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reason_id")
    private Long reasonId;

    @Column(name = "reason_code", length = 30, nullable = false)
    private String reasonCode;

    @Column(name = "reason_name", length = 100, nullable = false)
    private String reasonName;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "last_update_date")
    private Instant lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}
