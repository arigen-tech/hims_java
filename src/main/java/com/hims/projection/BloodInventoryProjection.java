package com.hims.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface BloodInventoryProjection {

    Long getInventoryId();

    String getUnitNo();

    Long getBloodGroupId();

    Integer getVolumeMl();

    LocalDate getExpiryDate();

    Long getComponentId();

    String getPreferred();
}
