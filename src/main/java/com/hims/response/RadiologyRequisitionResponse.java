package com.hims.response;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDate orderDate;
    private LocalDateTime orderTime;
    private String Department;
    private Long radOrderDtId;
    private String reportStatus;
    private String studyStatus;
    private LocalDate studyDate;
    private LocalDateTime studyTime;

}
