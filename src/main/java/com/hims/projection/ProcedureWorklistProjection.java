package com.hims.projection;

import java.time.LocalDateTime;

public interface ProcedureWorklistProjection {

    Long getProcedureHdId();

    Long getProcedureDtId();

    Long getPatientId();

    String getMobileNo();

    String getPatientName();

    Integer getAge();

    String getGender();

    String getDepartment();

    String getProcedure();

    Integer getCompletedSessions();

    Integer getTotalSessions();

    LocalDateTime getScheduledDateTime();

    String getAdvisedBy();

    String getBillingStatus();
}