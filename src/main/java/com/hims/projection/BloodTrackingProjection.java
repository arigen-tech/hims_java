package com.hims.projection;

import java.time.LocalDateTime;

public interface BloodTrackingProjection {
    Long getRequestDtId();
    String getRequestNo();
    Long getInpatientId();
    String getInpatientNo();
    Long getPatientId();
    String getPatientName();
    String getBloodGroup();
    Long getBloodGroupId();
    String getComponent();
    Long getComponentId();
    Integer getUnits();
    String getUrgency();
    String getRequestedWard();
    LocalDateTime getRequestedDateTime();
    LocalDateTime getRequiredByDateTime();
    String getRequestedBy();
    String getTrackingStatus();
}