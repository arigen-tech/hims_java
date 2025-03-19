package com.hims.request;

import lombok.Data;

@Data
public class UserDepartmentRequest {
    private Long userId;
    private Long departmentId;
    private String lastChgBy;
}
