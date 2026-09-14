package com.hims.projection;

import java.time.LocalDateTime;

public interface BloodTrackingProjection {
    String getRequestNo();
    Long getInpatientId();
    String getInpatientNo();
    Long getPatientId();
    String getPatientName();
    String getBloodGroup();
    String getComponent();
    Integer getUnits();
    String getUrgency();
    String getRequestedWard();
    LocalDateTime getRequestedDateTime();
    LocalDateTime getRequiredByDateTime();
    String getRequestedBy();
    String getTrackingStatus();
}