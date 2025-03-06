package com.hims.response;

import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
public class MasRoleResponse {
    private String id;
    private String roleCode;
    private String roleDesc;
    private Boolean isActive;
    private Instant createdOn;
    private Instant updatedOn;
}