package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
public class MasHospitalResponse {
    private Long id;
    private String hospitalCode;
    private String hospitalName;
    private String status;
    private String address;
    private String contactNumber;
    private String contactNumber2;
    private String lastChgBy;
    private LocalDate lastChgDate;
    private String lastChgTime;
    private Long countryId;
    private String countryName;
    private Long stateId;
    private String stateName;
    private Long districtId;
    private String districtName;
    private String pinCode;
    private String city;
    private String email;
    private String gstnNo;
    private String regCostApplicable;
    private String appCostApplicable;
    private String preConsultationAvailable;
    private BigDecimal registrationCost;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String executive1Contact;
    private String executive2Contact;
}