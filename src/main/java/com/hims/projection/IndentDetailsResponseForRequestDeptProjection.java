package com.hims.projection;

import java.math.BigDecimal;

public interface IndentDetailsResponseForRequestDeptProjection {

    Long getIndentTId();

    String getItemName();

    String getItemUnitName();

    BigDecimal getQtyRequested();

    BigDecimal getQtyApproved();

    BigDecimal getQtyReceived();

    String getReasonForIndent();

    BigDecimal getStoreAvailableStock();

    BigDecimal getCurrentDeptAvailableStock();
}