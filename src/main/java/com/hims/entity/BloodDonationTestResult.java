package com.hims.entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_donation_test_result", schema = "public")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodDonationTestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_test_id")
    private Long testResultId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_id", nullable = false)
    private BloodDonationHdr donation;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_test_id", nullable = false)
    private MasBloodTest test;


    @Column(name = "test_result", length = 20)
    private String result;


    @Column(name = "test_date")
    private LocalDate testDate;


    @Column(name = "remarks", length = 500)
    private String remarks;


    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 100)
    private String createdBy;
}