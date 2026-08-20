package com.hims.response;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MasOtTeamRoleResponse {

    private Long otTeamRoleId;
    private String roleCode;
    private String roleName;
    private String description;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
}
