package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
@Data
public class ValidatedResponse {
    private Long patientId;
    private String patientName;
    private String relation;
    private String patientGender;
    private String patientAge; // keep as String (e.g., "51 Years")
    private LocalDateTime collectedDate;
    private String orderNo;
    private LocalTime  collectedTime;
    private String department;
    private  String doctorName;

    List<ResultInvestigationResponse> resultInvestigationResponseList;
}
