package com.hims.projection;

import java.time.OffsetDateTime;

public interface UserDepartmentProjection {

    Long getId();
    Long getUserId();
    String getUsername();
    Long getDepartmentId();
    String getDepartmentName();
    String getLastChgBy();

}