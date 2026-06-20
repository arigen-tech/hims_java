package com.hims.m1.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class CurlLoggingUtil {

    private static final String CONTROLLER_CURL_LOGGED_ATTRIBUTE = "controllerCurlLogged";

    private final ObjectMapper objectMapper;

    public void logIncomingCurl(String fallbackEndpoint, Object requestBody) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes == null) {
            return;
        }

        HttpServletRequest request = attributes.getRequest();
        if (Boolean.TRUE.equals(request.getAttribute(CONTROLLER_CURL_LOGGED_ATTRIBUTE))) {
            return;
        }

        request.setAttribute(CONTROLLER_CURL_LOGGED_ATTRIBUTE, Boolean.TRUE);

        String method = request.getMethod();
        String url = request.getRequestURL().toString();
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) {
            url = url + "?" + request.getQueryString();
        }

        Map<String, String> headers = extractHeaders(request);
        String curl = buildCurl(method, url, headers, requestBody);

        log.info("[API-CURL][INCOMING][{}]\n{}",
                fallbackEndpoint != null ? fallbackEndpoint : request.getRequestURI(),
                curl);
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if ("content-length".equalsIgnoreCase(headerName)) {
                continue;
            }
            headers.put(headerName, request.getHeader(headerName));
        }

        return headers;
    }

    private String buildCurl(String method,
                             String url,
                             Map<String, String> headers,
                             Object requestBody) {
        StringBuilder curl = new StringBuilder()
                .append("curl --location --request ")
                .append(method)
                .append(" ")
                .append(quote(url));

        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getValue() == null) {
                continue;
            }
            curl.append(" \\\n  --header ")
                    .append(quote(header.getKey() + ": " + header.getValue()));
        }

        String serializedBody = serializeBody(requestBody);
        if (serializedBody != null && !serializedBody.isBlank()) {
            curl.append(" \\\n  --data ")
                    .append(quote(serializedBody));
        }

        return curl.toString();
    }

    private String serializeBody(Object requestBody) {
        if (requestBody == null) {
            return null;
        }

        if (requestBody instanceof String requestBodyString) {
            return requestBodyString;
        }

        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException exception) {
            log.warn("Unable to serialize request body for cURL logging: {}", exception.getMessage());
            return String.valueOf(requestBody);
        }
    }

    private String quote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }
}
