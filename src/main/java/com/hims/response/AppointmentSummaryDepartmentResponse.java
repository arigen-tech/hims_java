package com.hims.response;

import lombok.Data;

@Data
public class AppointmentSummaryDepartmentResponse {
    private Long departmentId;
    private String departmentName;
    private Long totalCount;
    private Long completedCount;
    private Long cancelledCount;
    private Long noShowCount;
    private Long pendingCount;

}
