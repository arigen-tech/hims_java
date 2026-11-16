package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class PacPendingResponse {
    private Long otBookingId;
    private String bookingNo;
    private Long otBookingRequestId;
    private Long patientId;
    private String patientName;
    private String uhidNo;
    private String ipNo;
    private String patientType;
    private String departmentName;
    private String primarySurgeonName;
    private String diagnosis;
    private LocalDate scheduledDate;
    private LocalTime scheduledStartTime;
    private LocalTime scheduledEndTime;
    private String otName;
    private String wardName;
    private String bedNumber;
    private List<SurgeryResponse> surgeries;

    @Data
    public static class SurgeryResponse {
        private Long surgeryId;
        private String surgeryName;
    }
}
