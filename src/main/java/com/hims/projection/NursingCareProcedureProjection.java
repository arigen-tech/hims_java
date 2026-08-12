package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface NursingCareProcedureProjection {
    Long getItemId();
    String getItemName();
    BigDecimal getQty();
    Long getProcedureTxnId();
    String getProcedureName();
    LocalDateTime getDateTime();
    String getUsedBy();
    String getBatchNo();
    LocalDate getExpiryDate();
    String getRemark();
}
