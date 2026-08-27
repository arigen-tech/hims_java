package com.hims.projection;

import java.math.BigDecimal;

public interface IndentDetailsWithAvlStockProjection {

    Long getIndentTId();

    String getItemName();

    String getItemUnitName();

    BigDecimal getQtyRequested();

    BigDecimal getQtyApproved();

    BigDecimal getQtyReceived();

    String getReasonForIndent();

    Long getAvailableStock();
}
