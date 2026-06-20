package com.hims.m1.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "REQUEST_LOG")
public class RequestLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LOG_ID")
    private Long logId;

    @Column(name = "CLIENT_IP")
    private String clientIp;

    @Column(name = "CREATED_AT")
    private LocalDateTime createdAt;

    @Column(name = "EXECUTION_TIME_MS")
    private Long executionTimeMs;

    @Column(name = "REQUEST_BODY", columnDefinition = "CLOB")
    private String requestBody;

    @Column(name = "REQUEST_HEADERS", columnDefinition = "CLOB")
    private String requestHeaders;

    @Column(name = "REQUEST_METHOD")
    private String requestMethod;

    @Column(name = "REQUEST_MODULE")
    private String requestModule;

    @Column(name = "REQUEST_URI")
    private String requestUri;

    @Column(name = "RESPONSE_STATUS")
    private Integer responseStatus;

    @Column(name = "USER_AGENT")
    private String userAgent;

    @Column(name = "USER_ID")
    private String userId;

    // Getters and setters
    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }
    public String getClientIp() { return clientIp; }
    public void setClientIp(String clientIp) { this.clientIp = clientIp; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }
    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    public String getRequestHeaders() { return requestHeaders; }
    public void setRequestHeaders(String requestHeaders) { this.requestHeaders = requestHeaders; }
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public String getRequestModule() { return requestModule; }
    public void setRequestModule(String requestModule) { this.requestModule = requestModule; }
    public String getRequestUri() { return requestUri; }
    public void setRequestUri(String requestUri) { this.requestUri = requestUri; }
    public Integer getResponseStatus() { return responseStatus; }
    public void setResponseStatus(Integer responseStatus) { this.responseStatus = responseStatus; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
