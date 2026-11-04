package com.hims.projection;

import java.time.LocalDateTime;

public interface OtTeamMemberProjection {
    Long getOtTeamMemberId();
    Long getOtBookingId();
    Long getOtTeamRoleId();
    String getRoleName();
    Long getUserId();
    String getUserFullName();
    String getIsPrimary();
    String getStatus();
    String getLastChgBy();
    LocalDateTime getLastChgDate();
}
