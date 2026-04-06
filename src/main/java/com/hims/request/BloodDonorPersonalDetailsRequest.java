package com.hims.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloodDonorPersonalDetailsRequest{
    private String firstName;
    private String middleName;
    private String lastName;
    private Long genderId;
    private LocalDate dateOfBirth;
    private String mobileNo;
    private Long bloodGroupId;
   // private Long donationTypeId;
    private Long relationId;
    private String addressLine1;
    private String addressLine2;
    private Long countryId;
    private Long stateId;
    private Long districtId;
    private String city;
    private String pinCode;

}
