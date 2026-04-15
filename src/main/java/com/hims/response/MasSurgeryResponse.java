package com.hims.response;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasSurgeryResponse {
    private Long surgeryId;
    private String surgeryCode;
    private String surgeryName;
    private Long departmentId;
    private String departmentName;
    private String surgeryLevel;
    private String isAnesthesiaRequired;
    private String status;

}