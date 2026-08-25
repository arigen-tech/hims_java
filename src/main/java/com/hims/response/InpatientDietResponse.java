package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class InpatientDietResponse {
    private Long inpatientId;
    private String patientName;
    private String uhid;
    private String age;
    private Long genderId;
    private String gender;
    private String mobileNo;
    private String admissionNo;
    private Long wardId;
    private String ward;
    private Long rooId;
    private String room;
    private Long bedId;
    private String bed;
    private String DietStatus;
    private Long dietTypeId;
    private String dietTypeName;
    private String specialInstruction;
    private LocalDateTime admissionDateTime;
    private String doctorName;



}
