package com.hims.m2.response;


import com.hims.m2.request.CareContextLinkRequest;
import lombok.Data;

@Data
public class CareContextLinkPending {
    private String requestId;
    private CareContextLinkRequest request;
    private String status = "TOKEN_REQUESTED"; // -> LINK_REQUESTED -> LINKED / FAILED
    private String message;
}
