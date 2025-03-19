package com.hims.response;

import lombok.Data;
import java.time.OffsetDateTime;

@Data
public class UserDepartmentResponse {
    private Long id;
    private Long userId;
    private String username;
    private Long departmentId;
    private String departmentName;
    private String lastChgBy;
    private OffsetDateTime lasUpdatedBy;
}
