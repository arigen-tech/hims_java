package com.hims.m2.response;
import lombok.Data;

@Data
public class CareContextLinkStatusResponse {
    private String requestId;
    private String state;     // TOKEN_REQUESTED / LINK_REQUESTED / LINKED / FAILED
    private String message;
}
