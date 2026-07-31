package com.hims.projection;

import java.time.Instant;

public interface MasApplicationProjection {
    String getAppId();
    String getName();
    String getParentId();
    String getUrl();
    Long getOrderNo();
    String getStatus();
    Instant getLastChgDate();
    Long getAppSequenceNo();
}
