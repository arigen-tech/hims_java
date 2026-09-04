package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OpdReportListResponse {
    private Long visitId;
    private Long patientId;
    private String patientName;
    private String mobileNumber;
    private String uhid;
    private String relation;
    private String gender;
    private String age;
    private String specialty;
    private String doctorName;
    private String nisNo;
    private String visitDateTime;
    private Long prescriptionHdId;
    private String prescriptionStatus;
}
