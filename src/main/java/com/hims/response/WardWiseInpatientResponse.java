package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class WardWiseInpatientResponse {
    private Long inpatientId;
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
    private Long roomId;
    private String room;
    private Long bedId;
    private String bed;
    private LocalDateTime admissionDateTime;

}
