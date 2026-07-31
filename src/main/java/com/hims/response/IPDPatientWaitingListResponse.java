package com.hims.response;

import lombok.Data;

import java.time.LocalDate;

@Data
public class IPDPatientWaitingListResponse {
    private Long opdPatientDetailsId;
    private Long visitId;
    private Long patientId;
    private String uhid;
    private String patientName;
    private String PatientMobileNo;
    private String age;
    private String gender;
    private LocalDate admissionAdviseDate;
    private String DoctorName;
    private Long departmentId;
    private String department;
    private Long wardId;
    private String wardName;
    private String admissionSource;
    private Long careLevelId;
    private String careLevel;
    private Long admissionWardCategoryId;
    private String  admissionWardCategoryName;


}
