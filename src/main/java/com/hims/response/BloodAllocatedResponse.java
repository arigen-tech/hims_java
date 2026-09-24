package com.hims.response;

import lombok.Data;

@Data
public class BloodAllocatedResponse {

    private Long requestHdId;
    private Long requestDtId;
    private String requestNo;

    private Long inpatientId;
    private String inpatientNo;
    private Integer age;
    private String gender;

    private Long patientId;
    private String patientName;

    private String bloodGroup;
    private String component;

    private Integer unitsRequired;
    private Integer unitsAllocated;

    private String ward;
    private String urgency;

    private String requestedOn;
    private String requiredBy;

    private String unitExpiryDate;
    private String unitVolume;
    private String unitNumber;
    private Long inventoryId;

    private Integer trackingStatusId;
}
