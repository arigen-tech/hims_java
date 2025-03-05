package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasTemplateRequest {
    private String templateCode;
    private String templateName;
    private Long lastChgBy;
    private Long hospitalId;
}