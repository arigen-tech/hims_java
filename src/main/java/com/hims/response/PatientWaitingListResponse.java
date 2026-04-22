package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientWaitingListResponse {
    private Long visitId;
    private Long patientId;
    private String tokenNo;
    private String patientNo;
    private String patientName;
    private String relation;
    private String age;
    private String gender;
    private String visitType;
    private String action;
}
