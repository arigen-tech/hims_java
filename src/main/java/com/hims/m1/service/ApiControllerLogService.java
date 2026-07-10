package com.hims.m1.service;

import com.hims.m1.Mapper.SaveApiLogMapper;
import com.hims.m1.config.RequestHeaderContext;
import com.hims.m1.util.CurlLoggingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class ApiControllerLogService {

    @Autowired
    private SaveApiLogMapper saveApiLogMapper;

    @Autowired
    private CurlLoggingUtil curlLoggingUtil;

    public <T> Mono<T> logMonoApi(String localEndPoint, Object requestBody, Mono<T> responseMono) {
        curlLoggingUtil.logIncomingCurl(localEndPoint, requestBody);
        return responseMono
                .doOnSuccess(response -> logSuccess(localEndPoint, requestBody, response))
                .doOnError(exception -> logFailure(localEndPoint, requestBody, exception));
    }

    public void logSuccess(String localEndPoint, Object requestBody, Object responseBody) {
        curlLoggingUtil.logIncomingCurl(localEndPoint, requestBody);
        saveApiLog(
                localEndPoint,
                resolveStatusCode(responseBody),
                requestBody,
                unwrapResponseBody(responseBody),
                resolveHospitalId(requestBody),
                resolveCreatedBy(requestBody)
        );
    }

    public void logFailure(String localEndPoint, Object requestBody, Throwable throwable) {
        curlLoggingUtil.logIncomingCurl(localEndPoint, requestBody);
        saveApiLog(
                localEndPoint,
                "500",
                requestBody,
                throwable != null ? throwable.getMessage() : "Unexpected error",
                resolveHospitalId(requestBody),
                resolveCreatedBy(requestBody)
        );
    }

    private void saveApiLog(String localEndPoint,
                            String statusCode,
                            Object requestBody,
                            Object responseBody,
                            Long hospitalId,
                            Long createdBy) {
        try {
            saveApiLogMapper.saveControllerApiLog(
                    localEndPoint,
                    statusCode,
                    requestBody,
                    responseBody,
                    hospitalId,
                    createdBy
            );
        } catch (Exception exception) {
            log.error("Failed to save API log for endpoint {}: {}", localEndPoint, exception.getMessage(), exception);
        }
    }

    private Long resolveCreatedBy(Object requestBody) {
        return RequestHeaderContext.getUserId();
    }

    private Long resolveHospitalId(Object requestBody) {
        return 0L;
    }

    private String resolveStatusCode(Object responseBody) {
        if (responseBody instanceof ResponseEntity<?> responseEntity) {
            return String.valueOf(responseEntity.getStatusCode().value());
        }
        return "200";
    }

    private Object unwrapResponseBody(Object responseBody) {
        if (responseBody instanceof ResponseEntity<?> responseEntity) {
            return responseEntity.getBody();
        }
        return responseBody;
    }
}
