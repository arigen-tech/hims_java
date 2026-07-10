package com.hims.projection;

import java.math.BigDecimal;

public interface BillingTemplateDetailProjection {

    Long getTemplateDetailsId();
    Long getItemId();
    String getItemName();
    String getUnit();
    String getType();
    BigDecimal getQty();
}