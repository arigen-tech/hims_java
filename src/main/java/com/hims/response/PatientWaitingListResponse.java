package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientWaitingListResponse {
    private Long visitId;
    private Long patientId;
    private String tokenNo;
    private String mobileNo;
    private String patientName;
    private String relation;
    private String age;
    private LocalDate dob;
    private String gender;
    private String visitType;
    private String action;
    private String departmentName;
}
