package com.hims.request;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class TemplateApplicationRequest {
    private Long templateId;
    private String appId;
    private Long lastChgBy;
    private Long orderNo;
}
