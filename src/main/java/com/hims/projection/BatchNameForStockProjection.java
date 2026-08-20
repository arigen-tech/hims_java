package com.hims.projection;

import java.time.LocalDate;

public interface BatchNameForStockProjection {

    Long getStockId();

    String getBatchName();

    LocalDate getDom();

    LocalDate getDoe();

    Long getBatchStock();

    Long getAvailableStock();

    Long getManufacturerId();
}