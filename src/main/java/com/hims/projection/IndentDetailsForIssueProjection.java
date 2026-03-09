package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface IndentDetailsForIssueProjection {

    Long getIndentTId();

    Long getItemId();
    String getItemName();
    String getPvmsNo();

    BigDecimal getRequestedQty();
    BigDecimal getApprovedQty();

    BigDecimal getAvailableStock();

    String getIssueStatus();
    String getReason();

    String getUnitAuName();
    Long getUnitAUid();

    String getBatchNo();
    BigDecimal getBatchAvailableStock();
    LocalDate getMfgDate();
    LocalDate getExpDate();
}