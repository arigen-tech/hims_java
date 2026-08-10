package com.hims.projection;

import java.time.LocalDateTime;

public interface MasProcedureProjection {

    Long getProcedureId();
    String getProcedureCode();
    String getProcedureName();
    String getStatus();
    String getLastChgBy();
    LocalDateTime getLastChgDate();


    String getOpdAllowed();
    String getIpdAllowed();
    String getIsNursing();

}