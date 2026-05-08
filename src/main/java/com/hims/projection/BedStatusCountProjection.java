package com.hims.projection;
public interface BedStatusCountProjection {

    Long getAvailable();

    Long getCleaning();

    Long getOccupied();

}