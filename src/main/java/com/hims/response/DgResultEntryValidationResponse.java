package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
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
    private String resultTime;
    private LocalDate resultDate;
    private Long mainChargeCode;
    private String mainChargeCodeName;
  //  private String doctorName;
    private Long subChargeCodeId;
    private String subChargeCodeName;
    private String enteredBy;
    private Long resultEntryHeaderId;
    private String validatedBy;
    private String resultEnteredBy;
    private List<ResultEntryInvestigationResponse> resultEntryInvestigationResponses;

}
