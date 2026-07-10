package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BloodDonorScreeningDetailsResponse {
    private Long donorId;
    private String donorCode;
    private String firstName;

    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private String mobileNo;
    private Long bloodGroupId;
    private String bloodGroup;
    private Long donationType;
    private String relation;
    private String donorScreeningStatus;
    private String currentDeferralReason;
    private LocalDate deferralUpToDate;
    private String addressLine1;
    private String addressLine2;
    private Long country;
    private String countryName;
    private Long state;
    private String stateName;
    private Long district;
    private String districtName;
    private String city;
    private String pinCode;

    private LocalDateTime createdDate;
    private String createdBy;
    private boolean isEligibleForDonation;
    private LocalDate nextEligibleDonationDate;

    List<BloodDonorPriviousScreening> bloodDonorPreviousScreenings;
}
