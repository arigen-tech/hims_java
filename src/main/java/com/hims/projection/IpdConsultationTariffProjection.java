package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IpdConsultationTariffProjection {

    Long getTariffId();

    Long getServiceCategoryId();
    String getServiceCategoryName();

    Long getVisitTypeId();
    String getVisitTypeName();

    Long getDepartmentId();
    String getDepartmentName();

    Long getDoctorId();
    String getDoctorName();

    BigDecimal getBaseTariff();

    LocalDateTime getFromDate();
    LocalDateTime getToDate();

    String getStatus();
}