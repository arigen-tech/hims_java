package com.hims.projection;
public interface PendingForOtSurgeryProjection {

    Long getOtBookingId();

    Long getOtBookingDtId();

    Long getOtBookingRequestId();

    Long getSurgeryId();

    String getSurgeryName();

    Long getSurgeryTypeId();

    String getSurgeryTypeName();

    String getSurgeryLevel();

    String getAnesthesiaRequired();
}
