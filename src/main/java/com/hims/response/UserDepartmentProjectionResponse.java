package com.hims.response;

import lombok.Data;

@Data
public class UserDepartmentProjectionResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long departmentId;
    private String departmentName;
    private String lastChgBy;

}
