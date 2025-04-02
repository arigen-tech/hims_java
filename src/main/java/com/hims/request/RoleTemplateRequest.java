package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleTemplateRequest {
    private Long roleId;
    private Long templateId;
    private String status;
    private Long lastChgBy;
}
