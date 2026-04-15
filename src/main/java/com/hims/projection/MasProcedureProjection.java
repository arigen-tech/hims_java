package com.hims.projection;

import java.time.LocalDateTime;

public interface MasProcedureProjection {

    Long getProcedureId();
    String getProcedureCode();
    String getProcedureName();
    String getStatus();
    String getLastChgBy();
    LocalDateTime getLastChgDate();

    Long getDepartmentId();
    String getDepartmentName();

    String getOpdAllowed();
    String getIpdAllowed();
    String getIsNursing();
    String getProcedureLevel();
}