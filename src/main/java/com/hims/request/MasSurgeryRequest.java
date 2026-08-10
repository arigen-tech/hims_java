package com.hims.request;

import lombok.Data;

@Data
public class MasSurgeryRequest {

    private String surgeryCode;
    private String surgeryName;
    private Long surgeryTypeId;
    private String surgeryLevel;
    private String isAnesthesiaRequired;
    private String isAdmissionRequired;
    private String isImplantRequired;
}