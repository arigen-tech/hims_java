package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface BloodDonorPreviousScreeningProjection {

    Long getScreeningId();
    LocalDate getScreeningDate();
    BigDecimal getHemoglobin();
    BigDecimal getWeight();
    BigDecimal getHeight();
    String getBp();
    Integer getPulse();
    BigDecimal getTemperature();
    String getScreeningResult();
    String getDeferralType();
    String getDeferralReason();
    String getConductedBy();
}