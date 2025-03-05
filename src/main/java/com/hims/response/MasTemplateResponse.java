package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class MasTemplateResponse {
    private Long id;
    private String templateCode;
    private String templateName;
    private String status;
    private Long lastChgBy;
    private Instant lastChgDate;
    private Long hospitalId;
}