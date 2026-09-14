package com.hims.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OpdVisitResponseDTO {
    private Long id;
    private Long tokenNo;
    private LocalDateTime visitDate;
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
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String visitType;
    private String displayPatientStatus;

    private LocalDateTime cancelledDateTime;
    private String cancelledBy;
}
