package com.hims.response;

import lombok.Data;

@Data
public class MasSurgeryResponse {

    private Long surgeryId;
    private String surgeryCode;
    private String surgeryName;
    private Long surgeryTypeId;
    private String surgeryTypeName;
    private String surgeryLevel;
    private String isAnesthesiaRequired;
    private String isAdmissionRequired;
    private String isImplantRequired;
    private String status;
}