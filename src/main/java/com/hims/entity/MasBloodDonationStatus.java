package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mas_blood_donation_status")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MasBloodDonationStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_status_id")
    private Long donationStatusId;

    @Column(name = "donation_status_code", length = 30)
    private String donationStatusCode;

    @Column(name = "donation_status_name", length = 100)
    private String donationStatusName;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "is_final", length = 1)
    private String isFinal;

    @Column(name = "status", length = 1)
    private String status;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;
}
