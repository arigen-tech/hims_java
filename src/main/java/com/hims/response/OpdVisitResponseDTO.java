package com.hims.response;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class OpdVisitResponseDTO {
    private Long id;
    private Long tokenNo;
    private Instant visitDate;
    private String visitStatus;
    private Long priority;
    private Long patientId;
    private String patientName;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private Long hospitalId;
    private String hospitalName;
    private Long BillingHdId;
    private String billingStatus;
    private Instant startTime;
    private Instant endTime;
    private String visitType;
    private String displayPatientStatus;

    private Instant cancelledDateTime;
    private String cancelledBy;
}
