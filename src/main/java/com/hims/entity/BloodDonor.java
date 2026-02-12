package com.hims.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "blood_donor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodDonor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donor_id")
    private Long donorId;

    @Column(name = "donor_code", length = 30, nullable = false)
    private String donorCode;

    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id", referencedColumnName = "id")
    private MasGender gender;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "mobile_no", length = 15, nullable = false)
    private String mobileNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blood_group_id", referencedColumnName = "blood_group_id")
    private MasBloodGroup bloodGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "donation_type_id", referencedColumnName = "donation_type_id")
    private MasBloodDonationType donationType;

    @Column(name = "relation", length = 50)
    private String relation;

    @Column(name = "donor_status", columnDefinition = "char(1)")
    private String donorStatus;

    @Column(name = "current_deferral_reason", length = 300)
    private String currentDeferralReason;

    @Column(name = "deferral_upto_date")
    private LocalDate deferralUptoDate;

    @Column(name = "address_line1", length = 200)
    private String addressLine1;

    @Column(name = "address_line2", length = 200)
    private String addressLine2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", referencedColumnName = "country_id")
    private MasCountry country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", referencedColumnName = "state_id")
    private MasState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", referencedColumnName = "id")
    private MasDistrict district;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "pincode", length = 10)
    private String pincode;

    @Column(name = "remarks", length = 300)
    private String remarks;

    @Column(name = "status", columnDefinition = "char(1)")
    private String status;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "created_by", length = 200)
    private String createdBy;
}