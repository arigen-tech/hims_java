package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasRoleRequest {
    private String roleCode;
    private String roleDesc;
    private Boolean isActive;
}