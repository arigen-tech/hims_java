package com.hims.projection;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface IpdPackageDetailsProjection {

    // Header fields
    Long getPackageId();
    String getPackageName();
    String getType();
    String getDepartmentName();
    Integer getStayDays();
    String getGeneratedInclusions();
    String getGeneratedExclusions();
    LocalDateTime getLastChgDate();
    String getStatus();

    // Child fields
    Long getInclusionId();
    Long getServiceCategoryId();
    String getServiceCategoryName();
    String getInclusionFlag();
    BigDecimal getLimitAmount();
    Integer getLimitQty();
}