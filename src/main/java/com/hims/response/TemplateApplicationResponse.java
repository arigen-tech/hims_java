package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TemplateApplicationResponse {
    private Long id;
    private Long templateId;
    private String appId;
    private String appName;
    private String status;
    private Instant lastChgDate;
    private Long lastChgBy;
    private Long orderNo;
}
