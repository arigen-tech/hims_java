package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class MasApplicationResponse {
    private String appId;
    private String name;
    private String parentId;
    private String url;
    private Long orderNo;
    private String status;
    private Instant lastChgDate;
    private Long appSequenceNo;
    private boolean assigned;
}