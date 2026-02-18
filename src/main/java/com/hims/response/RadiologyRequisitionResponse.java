package com.hims.response;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
public class RadiologyRequisitionResponse {
    private String accessionNo;
    private String uhidNo;
    private String patientName;
    private String age;
    private String gender;
    private String phoneNumber;
    private String modality;
    private String investigationName;
    private LocalDate orderDate;
    private Instant orderTime;
    private String Department;
    private Long radOrderDtId;
}
