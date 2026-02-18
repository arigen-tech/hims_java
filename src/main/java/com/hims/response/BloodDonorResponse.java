package com.hims.response;

import com.hims.entity.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class BloodDonorResponse {
    private Long donorId;
    private String donorCode;
    private String firstName;
    private String middleName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private String mobileNo;
    private Long bloodGroup;
    private Long donationType;
    private String relation;
    private String donorStatus;
    private String currentDeferralReason;
    private LocalDate deferralUptoDate;
    private String addressLine1;
    private String addressLine2;
    private Long country;
    private Long state;
    private Long district;
    private String city;
    private String pincode;
    private String remarks;
    private String status;
    private LocalDateTime createdDate;
    private String createdBy;
}
