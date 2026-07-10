package com.hims.projection;

public interface AppointmentSummaryDepartmentProjection {


    Long getDepartmentId();

    String getDepartmentName();

    Long getTotalCount();         // Total appointments (all statuses)

    Long getCompletedCount();     // visit_status = 'Y'

    Long getCancelledCount();     // visit_status = 'C'

    Long getNoShowCount();        // visit_status = 'X'

    Long getPendingCount();       // visit_status = 'N' (changed from waitingCount)

}
