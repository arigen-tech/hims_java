package com.hims.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "mas_ipd_billing_type", schema = "public")
public class MasIpdBillingType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "billing_type_id")
    private Long billingTypeId;

    @Column(name = "billing_type_name", length = 50)
    private String billingTypeName;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "last_updated_by", length = 100)
    private String lastUpdatedBy;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;

}