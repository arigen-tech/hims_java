package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BloodTrackingResponse {
    private Long inpatientId;
    private String requestNo;
    private String inpatientNo;
    private Long patientId;
    private String patientName;
    private String bloodGroup;
    private String component;
    private Integer units;
    private String urgency;
    private LocalDateTime requestedDateTime;
    private String requestedWard;
    private String requestedBy;
    private LocalDateTime requiredByDateTime;
    private String trackingStatus;

}
