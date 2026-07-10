package com.hims.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
public class BloodDonorCollectionDetailsResponse {
    private Long donorId;
    private String donorCode;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dateOfBirth;
    private String mobileNo;
    private Long bloodGroupId;
    private String bloodGroup;
    private String donorScreeningStatus;
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
    private Long screeningId;
    private LocalDate screeningDate;
    private BigDecimal hemoglobin;
    private BigDecimal weight;
    private BigDecimal height;
    private String bp;
    private Integer pulse;
    private BigDecimal temperature;

}
