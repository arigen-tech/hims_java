package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class ResultEntryHeaderForUpdateResponse {

    private Long resultEntryHeaderId;
    private String patientName;
    private String patientGender;
    private String patientAge;
    private String patientPhnNum;
    private String patientRelation;
    private String doctorName;
    private Integer orderHdId;
    private String orderNo;
    private LocalDate orderDate;
    private Instant orderTime;

}
