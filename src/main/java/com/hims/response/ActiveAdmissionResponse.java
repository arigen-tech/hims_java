package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActiveAdmissionResponse {
    private Long inpatientId;
    private String patientName;
    private String uhid;
    private String age;
    private Long genderId;
    private String gender;
    private String mobileNo;
    private String emergencyMobileNo;
    private String admissionNo;
    private Long wardId;
    private String ward;
    private Long rooId;
    private String room;
    private Long bedId;
    private String bed;
    private LocalDateTime admissionDateTime;
    private LocalDateTime dischargeDate;
    private Long categoryId;
    private String categoryName;
    private String doctorName;
    private String los;
    private String status;
    private String billingType;



}
