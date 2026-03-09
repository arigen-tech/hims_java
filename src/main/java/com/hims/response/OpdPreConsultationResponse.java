package com.hims.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpdPreConsultationResponse {
    private Long visitId;
    private Long patientId;
    private String patientName;
    private String age;
    private String gender;
    private String departmentName;
    private String departmentId;
    private String mobleNumber;
    private String visitType;
    private String doctorName;
    private String doctorId;
    private String appointmentDate;
    private String appointmentTime;
    private String tokenNumber;
}
