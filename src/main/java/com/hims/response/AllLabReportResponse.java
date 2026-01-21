package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AllLabReportResponse {

    private Long resultEntryHeaderId;
    private Long resultEntryDetailsId;
    private Long orderHdId;
    private String investigationName;
    private String patientName;
    private String phnNum;
    private String gender;
    private String age;
    private String unit;
    private String result;
    private String range;
    private String resultEnteredBy;
    private String resultValidatedBy;
    private LocalDate investigationDate;
    private Boolean inRange;

}
