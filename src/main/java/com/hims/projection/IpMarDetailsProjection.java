package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface IpMarDetailsProjection {
    Long getInpatientId();
    LocalDateTime getAdministrationTime();
    Long getItemId();
    String getNomenclature();
    String getRouteName();
    String getDose();
    BigDecimal getAdministeredQty();
    String getBatchNo();
    LocalDate getExpiryDate();
    String getAdministeredBy();
    String getRemarks();
}
