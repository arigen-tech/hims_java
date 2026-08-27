package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
public class OtRequest {
    private Long inpatientId;
    private Long patientId;
    private Long visitId;
    private Long hospitalId;
    private Long surgeryTypeId;
    private Long surgeryId;
    private String diagnosis;
    private Long departmentId;
    private Long primarySurgeonId;
    private String priority;
    private LocalDate preferredDate;
    private Long preferredOtId;
    private Integer expectedDuration;
    private String specialInstructions;
    private LocalTime preferredStartTime;
    private LocalTime preferredEndTime;




}
