package com.hims.m1.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Universal header holder for all incoming requests
 * Access headers using RequestHeaderContext.getHeaders()
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestHeader {
    
    private String userId;
    private String ipAddress;
    
}
