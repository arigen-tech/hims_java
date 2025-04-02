package com.hims.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoleTemplateResponse {
    private Long id;
    private Long roleId;
    private Long templateId;
    private String status;
    private Long lastChgBy;
}
