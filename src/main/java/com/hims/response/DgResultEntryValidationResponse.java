package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class DgResultEntryValidationResponse {

    private Long patientId;
    private String patientName;
    private Long relationId;
    private String relation;
    private String patientGender;
    private String patientAge;
    private String patientPhnNum;
    private String orderDate;
    private LocalDateTime collectedDate;
    private String orderNum;
    private LocalTime collectedTime;
    private String department;
    private Long mainChargeCode;
    private String doctorName;
    private Long subChargeCodeId;
    private String subChargeCodeName;
    private String enteredBy;
    private Long visitId;
    private Long resultEntryHeaderId;
    private List<ResultEntryInvestigationResponse> resultEntryInvestigationResponses;

}
