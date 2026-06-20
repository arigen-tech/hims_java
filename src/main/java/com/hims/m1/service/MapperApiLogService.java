package com.hims.m1.service;

import com.hims.m1.Mapper.SaveApiLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class MapperApiLogService {

    @Autowired
    private SaveApiLogMapper saveApiLogMapper;

    public <T> Mono<T> logAbdmApi(String localEndPoint,
                                  String serverEndPoint,
                                  Object requestBody,
                                  Mono<T> responseMono) {
        return responseMono
                .doOnSuccess(response -> saveSuccess(localEndPoint, serverEndPoint, requestBody, response))
                .doOnError(error -> saveFailure(localEndPoint, serverEndPoint, requestBody, error));
    }

    private void saveSuccess(String localEndPoint, String serverEndPoint, Object requestBody, Object responseBody) {
        try {
            saveApiLogMapper.saveMapperApiLog(
                    serverEndPoint,
                    localEndPoint,
                    "200",
                    requestBody,
                    responseBody,
                    null,
                    null
            );
        } catch (Exception exception) {
            log.error("Failed to save mapper success log for {}: {}", localEndPoint, exception.getMessage(), exception);
        }
    }

    private void saveFailure(String localEndPoint, String serverEndPoint, Object requestBody, Throwable throwable) {
        try {
            String statusCode = "500";
            Object responseBody = throwable != null ? throwable.getMessage() : "Unexpected mapper exception";

            if (throwable instanceof WebClientResponseException webClientResponseException) {
                statusCode = String.valueOf(webClientResponseException.getStatusCode().value());
                responseBody = webClientResponseException.getResponseBodyAsString();
            }

            saveApiLogMapper.saveMapperApiLog(
                    serverEndPoint,
                    localEndPoint,
                    statusCode,
                    requestBody,
                    responseBody,
                    null,
                    null
            );
        } catch (Exception exception) {
            log.error("Failed to save mapper error log for {}: {}", localEndPoint, exception.getMessage(), exception);
        }
    }
}
