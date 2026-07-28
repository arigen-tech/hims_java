package com.hims.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "mas_discharge_reason", schema = "public")
public class MasDischargeReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discharge_reason_id")
    private Long dischargeReasonId;

    @Column(name = "reason_code", length = 20)
    private String reasonCode;

    @Column(name = "reason_name", length = 100)
    private String reasonName;

    @Column(name = "status", length = 1)
    private String status = "Y";

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;
}