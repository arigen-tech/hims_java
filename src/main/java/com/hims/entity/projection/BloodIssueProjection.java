package com.hims.entity.projection;

import java.time.LocalDateTime;

public interface BloodIssueProjection {
    Long getRequestHdId();
    Long getRequestDtId();
    String getRequestNo();
    String getInpatientNo();
    String getPatientName();
    String getBloodGroup();
    String getComponent();
    Integer getUnitsReserved();
    String getRequestDept();
    String getUrgency();
    LocalDateTime getRequiredBy();
    LocalDateTime getReservedOn();
    Long getInventoryId();

}
