package com.hims.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasUserDepartmentResponse {
    private Long userDepartmentId;
    private Long userId;
    private String userName;
    private Long departmentId;
    private String departmentName;

    public MasUserDepartmentResponse(Long userDepartmentId, Long userId, String userName, Long departmentId, String departmentName) {
        this.userDepartmentId = userDepartmentId;
        this.userId = userId;
        this.userName = userName;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
    }
}
