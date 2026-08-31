package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActiveAdmissionOtResponse {
    private Long inpatientId;
    private Long visitId;
    private Long patientId;
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
    private String diagnosis;
    private LocalDateTime admissionDateTime;
    private String doctorName;



}
