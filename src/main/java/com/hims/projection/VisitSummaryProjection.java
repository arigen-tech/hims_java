package com.hims.projection;

public interface VisitSummaryProjection {

        Long getDoctorId();
        String getDoctorName();
        Long getDepartmentId();
        String getDepartmentName();
        Long getTotalVisits();
        Long getCompletedCount();
        Long getWaitingCount();
        Long getCancelledCount();
        Long getInProgressCount();

}
