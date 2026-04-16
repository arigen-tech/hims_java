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
@Table(name = "blood_donation_hdr", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodDonationHdr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_id")
    private Long donationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donor_id")
    private BloodDonor donorId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screening_id")
    private BloodDonorScreening screeningId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_type_id")
    private MasBloodDonationType donationTypeId;

    @Column(name = "patient_id")
    private Long patientId;

    @Column(name = "inpatient_id")
    private Long inpatientId;

    @Column(name = "bag_number", length = 50)
    private String bagNumber;

    @Column(name = "donation_datetime")
    private LocalDateTime donationDatetime;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_type_id")
    private MasBloodCollectionType collectionTypeId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bag_type_id")
    private MasBloodBagType bagTypeId;

    @Column(name = "total_collected_volume_ml")
    private Integer totalCollectedVolumeMl;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_status_id")
    private MasBloodDonationStatus donationStatusId;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_failure_reason")
    private MasComponentFailureReason componentFailureReason;

    @Column(name = "component_generation_datetime")
    private LocalDateTime componentGenerationDatetime;

    @Column(name = "testing_datetime")
    private LocalDateTime testingDatetime;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;
}