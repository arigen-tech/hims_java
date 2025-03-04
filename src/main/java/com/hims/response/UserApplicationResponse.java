package com.hims.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class UserApplicationResponse {
    private Long id;
    private String userAppName;
    private String url;
    private String status;
    private Long lastChgBy;
    private Instant lastChgDate;
}