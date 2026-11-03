package com.hims.projection;
public interface PendingForOtSurgeryProjection {

    Long getOtBookingDtId();

    Long getOtBookingRequestId();

    Long getSurgeryId();

    String getSurgeryName();

    Long getSurgeryTypeId();

    String getSurgeryTypeName();

    String getSurgeryLevel();

    String getAnesthesiaRequired();
}
