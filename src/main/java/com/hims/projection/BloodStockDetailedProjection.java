package com.hims.projection;

import java.time.LocalDate;

public interface BloodStockDetailedProjection {
    String getUnitNo();
    String getComponent();
    String getBloodGroup();
    Integer getVolumeMl();
    LocalDate getExpiryDate();
    String getStatus();
    String getReservedFor();
}