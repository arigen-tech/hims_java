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

    Integer getAllocatedUnits();

    Integer getFulfilledUnits();

    Integer getFailedUnits();

    Integer getPendingUnits();

    Long[] getAllocationIds();

    Integer getAcknowledgedUnits();

    Integer getPendingAcknowledgementUnits();

    Boolean getCanAcknowledge();

    String getUrgency();

    LocalDateTime getRequestedDateTime();

    String getRequestedWard();

    String getRequestedBy();

    LocalDateTime getRequiredByDateTime();

    String getTrackingStatus();
}