package com.hims.request;

import lombok.Data;

@Data
public class MasHospitalRequest {
    private String hospitalCode;
    private String hospitalName;
    private String status;
    private String address;
    private String contactNumber;
    private String contactNumber2;
    private Long countryId;
    private Long stateId;
    private Long districtId;
    private String city;
    private String pinCode;
    private String email;
    private String gstnNo;
    private String regCostApplicable;
    private String appCostApplicable;
    private String preConsultationAvailable;
    private String lastChgBy;
}