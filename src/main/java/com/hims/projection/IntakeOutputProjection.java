package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IntakeOutputProjection {

    Long getInpatientId();

    Long getIoEntryId();

    Long getIntakeTypeId();

    String getIntakeTypeName();

    Long getIntakeItemId();

    String getIntakeItemName();

    LocalDateTime getDateTime();

    BigDecimal getIntakeQuantity();

    Long getOutputTypeId();

    String getOutputName();

    String getIoType();
}