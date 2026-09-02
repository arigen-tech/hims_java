package com.hims.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
public class PendingForOtResponse {
    private Long otBookingRequestId;
    private Long inpatientId;
    private Long visitId;
    private Long patientId;
    private String patientName;
    private String uhid;
    private String age;
    private Long genderId;
    private String gender;
    private String mobileNo;
    private String admissionNo;
    private Long surgeonId;
    private String surgeonName;
    private String patientType;
    private Long otId;
    private String otName;
    private String priority;
    private LocalDate requestedDate;
    private LocalTime requestedTime;
    private String requestedBy;
    private String requestedNo;
     List<SurgeryResponse> surgeryResponses;
    @Data
    public static class SurgeryResponse{
        private Long otBookingDtId;
        private Long surgeryId;
        private String surgeryName;
    }





}
