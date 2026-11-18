package com.hims.projection;

public interface ComponentWiseStockProjection {

    String getComponentCode();

    String getBloodGroupName();

    Long getAvailableUnits();

    Long getReservedUnits();

    Long getAllocatedUnits();

    Long getExpiredUnits();
}
