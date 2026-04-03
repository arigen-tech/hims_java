package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LabInvestigationsReportResponse {

    private Long resultEntryHeaderId;
    private Long resultEntryDetailsId;
    private Integer orderHdId;
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


}
