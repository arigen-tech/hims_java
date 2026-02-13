package com.hims.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class OpdPatientDetailResponseDTO {

    private Long opdPatientDetailsId;

    private String height;
    private String weight;
    private String pulse;
    private String temperature;
    private String bmi;
    private String bpSystolic;
    private String bpDiastolic;

    private String presentComplaints;
    private String workingDiag;
    private String icdDiag;

    private Instant opdDate;

    private Long patientId;
    private String patientName;

    private Long visitId;

    private Long departmentId;
    private String departmentName;

    private Long doctorId;
    private String doctorName;

    private String followUpFlag;
    private Long followUpDays;
    private Instant followUpDate;

    private String admissionFlag;
}
