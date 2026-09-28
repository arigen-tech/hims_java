package com.hims.entity.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface BloodAllocatedProjection {
    Long getRequestHdId();
    Long getRequestDtId();
    String getRequestNo();
    Long getInpatientId();
    LocalDate getDob();
    String getGender();
    String getInpatientNo();
    Long getPatientId();
    String getPatientName();
    String getBloodGroup();
    String getComponent();
    Integer getUnitsRequired();
    Integer getUnitsAllocated();
    String getWard();
    String getUrgency();
    LocalDateTime getRequestedOn();
    LocalDateTime getRequiredBy();
    Integer getTrackingStatusId();
    String getUnitNumber();
    LocalDate getUnitExpiry();
    String getUnitVolume();
    Long getInventoryId();
    }