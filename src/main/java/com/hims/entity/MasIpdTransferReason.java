package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_ipd_transfer_reason")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MasIpdTransferReason {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_reason_id")
    private Long transferReasonId;

    @Column(name = "transfer_reason_name", length = 50)
    private String transferReasonName;

    @Column(name = "description", length = 200)
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