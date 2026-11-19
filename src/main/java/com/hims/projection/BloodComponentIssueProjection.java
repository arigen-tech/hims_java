package com.hims.projection;
import java.time.LocalDateTime;

public interface BloodComponentIssueProjection {

    Long getRequestDtId();

    String getWardName();

    String getComponentName();

    String getComponentCode();

    String getBloodGroup();

    Integer getUnitsIssued();

    LocalDateTime getIssueDate();

    String getIssuedBy();

    String getPatientName();
}