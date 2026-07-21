package com.hims.projection;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IpVitalsProjection {

    Long getVitalId();

    Long getInpatientId();

    LocalDateTime getObservationDatetime();

    BigDecimal getTemperature();

    Integer getPulse();

    Integer getBpSystolic();

    Integer getBpDiastolic();

    Integer getRespiration();

    BigDecimal getSpo2();

    Integer getPainScore();
}