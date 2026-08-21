package com.hims.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtBookingRequestHdDto {

    private Long otBookingRequestId;
    private String requestNo;
    private Long patientId;
    private String requestSource;
    private Long visitId;
    private Long departmentId;
    private Long primarySurgeonId;
    private Long icdCodeId;
    private String diagnosis;
    private String priority;
    private Long preferredOtId;
    private LocalDate preferredDate;
    private LocalTime preferredStartTime;
    private LocalTime preferredEndTime;
    private String specialInstruction;
    private Long bookingStatusId;
    private String requestedBy;
    private LocalDateTime requestedDate;
    private String reviewedBy;
    private LocalDateTime reviewedDate;
    private String rejectionRemarks;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
    // DT records
    private List<OtBookingRequestDtDto> surgeryDetails;
}