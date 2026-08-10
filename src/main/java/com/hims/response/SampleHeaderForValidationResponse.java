package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SampleHeaderForValidationResponse {

    private Long sampleCollectionHeaderId;
    private LocalDateTime collectionTime;
    private String orderNumber;
    private LocalDate orderDate;
    private String patientName;
    private String mobileNo;
    private String gender;
    private String age;
    private Long subChargeCodeId;
    private String subChargeCodeName;
    private String doctorName;
    private String patientRelation;
    private String collectedBy;
    private String departmentName;
    private Integer dgOrderHdId;



}
