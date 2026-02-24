package com.hims.projection;

import java.time.Instant;
import java.time.LocalDate;

public interface RadiologyProjection {

    Long getRadOrderdtId();
    String getOrderAccessionNo();
    String getUhid();
    String getPatientName();
    String getAge();
    String getGender();
    String getMobileNo();
    Long getModalityId();
    String getModalityName();
    Long getInvestigationId();
    String getInvestigationName();
    Instant getOrderTime();
    LocalDate getOrderDate();
    String getDepartment();
    String getReportStatus();
    String getStudyStatus();
}
