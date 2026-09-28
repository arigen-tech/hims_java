package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BloodTrackingResponse {

    private Long requestDtId;
    private Long inpatientId;
    private String requestNo;
    private String inpatientNo;
    private Long patientId;
    private String patientName;
    private String bloodGroup;
    private Long bloodGroupId;
    private String component;
    private Long componentId;

    private Integer units;
    private Integer allocatedUnits;
    private Integer fulfilledUnits;
    private Integer failedUnits;
    private Integer pendingUnits;

    private Long[] allocationIds;

    private String urgency;
    private LocalDateTime requestedDateTime;
    private String requestedWard;
    private String requestedBy;
    private LocalDateTime requiredByDateTime;
    private String trackingStatus;

    private Integer acknowledgedUnits;
    private Integer pendingAcknowledgementUnits;
    private String acknowledgementStatus;
    private Boolean canAcknowledge;
    private Integer acceptedUnits;
}