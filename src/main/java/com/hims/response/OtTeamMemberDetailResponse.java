package com.hims.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OtTeamMemberDetailResponse {
    private Long otTeamMemberId;
    private Long otTeamRoleId;
    private String roleName;
    private Long userId;
    private String userFullName;
    private String isPrimary;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
}
