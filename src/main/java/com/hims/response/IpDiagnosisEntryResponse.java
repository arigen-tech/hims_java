package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IpDiagnosisEntryResponse {
    private Long inpatientId;
    private Long icdId;
    private String icdCode;
    private String icdName;
    private String remark;
    private String diagnosisType;
    private String status;
    private String diagnosis;
    private LocalDateTime dateTime;

}
