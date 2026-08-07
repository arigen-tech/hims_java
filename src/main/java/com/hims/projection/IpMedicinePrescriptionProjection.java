package com.hims.projection;

import java.time.LocalDateTime;

public interface IpMedicinePrescriptionProjection {

    Long getPrescriptionId();

    Long getInpatientId();

    Long getItemId();

    String getItemName();

    Long getRouteId();

    String getRouteName();

    String getDose();

    Long getFrequencyId();

    String getFrequencyName();

    LocalDateTime getStartDate();

    LocalDateTime getStopDate();

    String getAdministratedBy();
}
