package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
@Data
public class ResultResponse {
    private Long patientId;
    private String patientName;
    private String relation;
    private Long relationId;
    private String patientGender;
    private String patientAge; // keep as String (e.g., "51 Years")
    private String patientPhoneNo;
    private String orderDate;
    private LocalDateTime collectedDate;
    private String orderNo;
    private LocalTime  collectedTime;
    private String department;
//    private Long departmentId;
    private Long mainChargeCodeId;
    private  String doctorName;
    private Long subChargeCodeId;
    private String subChargeCodeName;
    private String enteredBy;
    private Long visitId;
    private Long sampleCollectionHeaderId;

    List<ResultInvestigationResponse> resultInvestigationResponseList;
}
