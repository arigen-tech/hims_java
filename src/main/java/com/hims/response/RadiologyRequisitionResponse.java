package com.hims.response;

import lombok.Data;

import java.time.Instant;

@Data
public class RadiologyRequisitionResponse {
    private String accessionNo;
    private String uhidNo;
    private String patientName;
    private String age;
    private String gender;
    private String phoneNumber;
    private String modality;
    private Long modalityId;
    private String investigationName;
    private String orderDate;
    private String orderTime;
    private String Department;
    private Long radOrderDtId;
    private String reportStatus;
    private String studyStatus;
    private String studyDate;
    private String studyTime;
    private Long hospitalId;

}
