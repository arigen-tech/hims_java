package com.hims.response;

import lombok.Data;

@Data
public class PatientIdResponse {
    private Long patientId;
    private String patientName;
    private String patientPhoneNumber;
    private String age;
    private String gender;
    private String relation;
}
