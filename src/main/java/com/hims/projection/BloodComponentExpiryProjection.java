package com.hims.projection;
import java.time.LocalDate;

public interface BloodComponentExpiryProjection {

    Long getInventoryId();

    String getUnitNo();

    String getComponentCode();

    String getComponentName();

    String getBloodGroup();

    LocalDate getExpiryDate();

    String getInventoryStatus();
}
