package com.hims.response;
import lombok.Data;

@Data
public class IpDischargeMedicationResponse {
    private Long medicationId;
    private String medicineName;
    private String dosage;
    private String frequency;
    private Integer durationDays;
    private Integer totalDoses;
    private String route;
    private String instruction;
}
