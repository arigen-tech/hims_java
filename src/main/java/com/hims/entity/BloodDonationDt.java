package com.hims.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_donation_dt", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodDonationDt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_dt_id")
    private Long donationDtId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_hd_id")
    private BloodDonationHdr donationHdId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_id")
    private MasBloodComponent componentId;

    @Column(name = "unit_no", length = 50)
    private String unitNo;

    @Column(name = "volume_ml")
    private Integer volumeMl;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_status")
    private MasBloodDonationStatus componentStatus;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;
}