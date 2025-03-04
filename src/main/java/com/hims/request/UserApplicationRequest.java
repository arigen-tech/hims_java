package com.hims.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserApplicationRequest {
    private String userAppName;
    private String url;
    private String status;
    private Long lastChgBy;
}