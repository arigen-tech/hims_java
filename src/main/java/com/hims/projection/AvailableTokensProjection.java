package com.hims.projection;

public interface AvailableTokensProjection {
    Integer getStartToken();
    Integer getTotalInterval();
    Integer getTotalToken();
    Integer getTotalOnlineToken();
    Integer getTimeTaken();
    String getStartTime();
    String getEndTime();
}
