package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;
@Data
public class ResultEntryUpdateResponse {
    private Long orderHdId;
    private String orderNo;
    private String orderDate;
    private String orderTime;
    private Long patientId;
    private String patientName;
    private Long relationId;
    private String relation;
    private String patientGender;
    private String patientAge;
    private String patientPhnNum;
    List<ResultEntryUpdateHeaderResponse> resultEntryUpdateHeaderResponses;
}
