package com.hims.projection;

import java.time.LocalDateTime;

public interface BloodTrackingProjection {

    Long getInpatientId();

    String getInpatientNo();

    Long getPatientId();

    String getPatientName();

    String getBloodGroup();

    String getComponent();

    Integer getUnits();

    String getUrgency();

    LocalDateTime getRequestedDateTime();

    String getRequestedBy();

    String getTrackingStatus();
}