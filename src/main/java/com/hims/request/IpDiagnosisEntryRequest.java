package com.hims.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IpDiagnosisEntryRequest {
    private Long inpatientId;
    private Long patientId;
    private Long departmentId;
    private String diagnosisType;
    private String diagnosisText;
    private String status;
    private LocalDateTime dateTime;
    private String remark;
    private Long icdId;


}
