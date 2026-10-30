package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class OtBookingHistoryResponse {
    private Long otBookingId;
    private Long otBookingRequestId;
    private Long otBookingRequestDtId;
    private Long patientId;
    private String uhidNo;
    private String patientName;
    private Long departmentId;
    private String departmentName;
    private Long surgeryId;
    private String surgeryName;
    private String surgeryLevel;
    private String anesthesiaRequired;
    private LocalDate preferredDate;
    private LocalTime preferredStartTime;
    private LocalTime preferredEndTime;
    private Long otId;
    private String otName;
    private String pacRequired;
    private String pacDone;
    private LocalDateTime bookedDate;
    private String bookingNo;
    private Long primarySurgeonId;
    private String surgeonName;
    private LocalDateTime cancelledDate;
    private String cancelledBy;
    private Long bookingStatusId;
    private String statusName;
}
