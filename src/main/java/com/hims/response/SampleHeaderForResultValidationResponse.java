package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@AllArgsConstructor
public class SampleHeaderForResultValidationResponse {

    private Long resultEntryHeaderId;
    private String resultTime;
    private LocalDate resultDate;
    private String patientName;
    private String patientGender;
    private String patientAge;
    private String patientPhnNum;
    private String patientRelation;
    private String mainChargeCodeName;
    private String doctorName;
    private Long subChargeCodeId;
    private String subChargeCodeName;
    private String resultEnteredBy;
    private Long orderHdId;
    private String orderNo;
}
