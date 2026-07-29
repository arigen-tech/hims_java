package com.hims.request;

import lombok.Data;

@Data
public class IpDischargeMedicationRequest {

    private String medicineName;
    private String dosage;
    private String frequency;
    private Integer totalDoses;
    private String route;
    private String instruction;
}