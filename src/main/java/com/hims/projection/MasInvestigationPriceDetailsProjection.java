package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MasInvestigationPriceDetailsProjection {
    Long getId();
    Long getInvestigationId();
    String getInvestigationName();
    LocalDate getFromDt();
    LocalDate getToDt();
    BigDecimal getPrice();
}
