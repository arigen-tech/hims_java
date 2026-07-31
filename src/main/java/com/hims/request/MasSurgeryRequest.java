package com.hims.request;
import lombok.Data;

@Data
public class MasSurgeryRequest {

    private String surgeryCode;
    private String surgeryName;
    private Long departmentId;
    private String surgeryLevel;
    private String isAnesthesiaRequired;
}