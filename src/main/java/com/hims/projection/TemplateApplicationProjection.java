package com.hims.projection;

import java.time.Instant;

public interface TemplateApplicationProjection {
    Long getId();
    Long getTemplateId();
    String getAppId();
    String getAppName();
    String getStatus();
    Instant getLastChgDate();
    Long getLastChgBy();
    Long getOrderNo();
    String getParentId();
}