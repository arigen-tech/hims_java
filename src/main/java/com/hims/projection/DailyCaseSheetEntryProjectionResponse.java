package com.hims.projection;

import java.time.LocalDateTime;

public interface DailyCaseSheetEntryProjectionResponse {

    Long getCaseSheetEntryId();

    Long getInpatient();

    String getNotes();

    String getInvestigation();

    String getMedicines();

    String getProcedure();

    String getPlan();

    String getFollowUp();

    LocalDateTime getVisitDateTime();


    Long getDoctorId();

    String getDoctorName();

    Long getDepartmentId();

    String getDepartmentName();
}
