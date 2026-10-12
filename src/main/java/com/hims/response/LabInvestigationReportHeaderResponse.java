package com.hims.response;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LabInvestigationReportHeaderResponse {

    private Long resultEntryHeaderId;
    private Long orderHdId;
    private LocalDate resultDate;
    private String resultNo;
    private String remarks;
    private String patientName;
    private Long patientId;
    private String phnNum;
    private String gender;
    private String age;
    private String resultEnteredBy;
    private String resultValidatedBy;
}