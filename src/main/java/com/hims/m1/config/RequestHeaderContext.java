package com.hims.m1.config;

/**
 * Thread-local storage for request headers
 * Accessible throughout the request lifecycle
 */
public class RequestHeaderContext {
    
    private static final ThreadLocal<RequestHeader> headerContext = new ThreadLocal<>();
    
    public static void setHeaders(RequestHeader headers) {
        headerContext.set(headers);
    }
    
    public static RequestHeader getHeaders() {
        RequestHeader headers = headerContext.get();
        return headers != null ? headers : new RequestHeader();
    }
    
    public static Long getUserId() {
//        RequestHeader headers = getHeaders();
//        return Long.valueOf(headers.getUserId());

        RequestHeader headers = getHeaders();
        if (headers == null || headers.getUserId() == null) {
            return null;
        }
        try {
            return Long.valueOf(headers.getUserId());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    

    public static String getIpAddress() {
        RequestHeader headers = getHeaders();
        return headers.getIpAddress();
    }
    
    public static void clear() {
        headerContext.remove();
    }
}
