package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_blood_donation_type", schema = "public")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasBloodDonationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_type_id")
    private Long donationTypeId;

    @Column(name = "donation_type_code", length = 20)
    private String donationTypeCode;

    @Column(name = "donation_type_name", length = 50)
    private String donationTypeName;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_by", length = 200)
    private String createdBy;

    @Column(name = "last_updated_by", length = 200)
    private String lastUpdatedBy;

    @Column(name = "last_update_date")
    private LocalDateTime lastUpdateDate;
}
