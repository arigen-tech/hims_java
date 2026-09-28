package com.hims.request;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class BloodCrossmatchRequest {

    private Long requestHdId;
    private Long requestDtId;
    private Long inpatientId;
    private Long patientId;
    private Long crossmatchTypeId;
    private Boolean isEmergency;
    private LocalDateTime sampleReceivedDatetime;
    private LocalDateTime crossmatchDatetime;
    private String overallResult;
    private String remarks;

    private List<BloodCrossmatchUnitRequest> units;
}
