package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SampleHeaderForResultEntryResponse {

    private Long sampleCollectionHeaderId;
    private Long patientId;
    private Long visitId;
    private String patientName;
    private  String mobileNumber;
    private Long relationId;
    private String relation;
    private  Long patientGenderId;
    private String patientGender;
    private String patientAge;
    private LocalDate orderDate;
    private Instant orderTime;
    private LocalDateTime collectedDate;
    private String orderNo;
    private LocalDateTime collectedTime;
    private String collectedBy;
    private LocalDate validatedDate;
    private Instant validatedTime;
    private String validatedBy;
    private String department;
    private  String doctorName;
    private Long mainChargeCodeId;
    private String mainChargeCodeName;
    private Long subChargeCodeId;
    private String subChargeCodeName;
    private Long inpatientId;

}
