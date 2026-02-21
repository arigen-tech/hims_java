package com.hims.response;

import lombok.Data;

@Data
public class ResultForInvestigationResponse {
    private Long patientId;
    private String patientName;
    private String age;
    private String investigationName;
    private String result;
    private String normalRange;
}
