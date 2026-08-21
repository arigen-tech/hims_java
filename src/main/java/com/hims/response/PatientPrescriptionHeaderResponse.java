package com.hims.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PatientPrescriptionHeaderResponse {

    private Long prescriptionHeaderId;
    private String prescriptionNo;
    private LocalDateTime prescriptionDate;
    private String patientName;
    private String uhidNo;
    private String mobileNumber;
    private String age;
    private String gender;
    private String departmentName;
    private String doctorName;
}
