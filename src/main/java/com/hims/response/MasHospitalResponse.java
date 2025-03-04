package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class MasHospitalResponse {
    private Long id;
    private String hospitalCode;
    private String hospitalName;
    private String status;
    private String address;
    private String contactNumber;
    private String lastChgBy;
    private Instant lastChgDate;
    private String lastChgTime;
    private String pinCode;
    private String regCostApplicable;
    private String appCostApplicable;
    private String preConsultationAvailable;

    private Long countryId;
    private String countryName;

    private Long stateId;
    private String stateName;

    private Long districtId;
    private String districtName;
}
