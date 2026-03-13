package com.hims.response;

import lombok.Data;

@Data
public class AppointmentSummaryDoctorResponse {
    private Long doctorId;
    private String doctorName;
    private Long totalCount;
    private Long completedCount;
    private Long cancelledCount;
    private Long noShowCount;
    private Long pendingCount;
}
