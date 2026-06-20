package com.hims.m1.exception;

import com.hims.m1.apiResponse.ApiResponse;
import com.hims.m1.apiResponse.ResponseUtils;
import com.hims.m1.enumData.ErrorCodeEnum;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.concurrent.TimeoutException;

@RestControllerAdvice(basePackages = "com.hims.m1.controller")
public class M1GlobalExceptionHandler {

    // Not found handler
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(HeaderValidationException.class)
    public ResponseEntity<ApiResponse<String>> handleHeaderValidationException(HeaderValidationException ex) {
        ApiResponse<String> response = new ApiResponse<>();
        response.setStatus(false);
        response.setError_code(ex.getErrorCode());
        response.setMessage(ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }




    // Network issues (DNS, connection refused, SSL, etc.)
    @ExceptionHandler(WebClientRequestException.class)
    public ResponseEntity<ApiResponse<String>> handleWebClientRequestException(
            WebClientRequestException ex) {

        ApiResponse<String> response = new ApiResponse<>();
        response.setStatus(false);
        response.setError_code(ErrorCodeEnum.getCode("NETWORK_ERROR"));
        response.setMessage("Unable to connect to ABDM services");

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    // Timeout
    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ApiResponse<String>> handleTimeoutException(
            TimeoutException ex) {

        ApiResponse<String> response = new ApiResponse<>();
        response.setStatus(false);
        response.setError_code(ErrorCodeEnum.getCode("TIMEOUT"));
        response.setMessage("ABDM request timed out");

        return ResponseEntity
                .status(HttpStatus.GATEWAY_TIMEOUT)
                .body(response);
    }

    // Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGeneralException(Exception ex) {

        ApiResponse<String> response = new ApiResponse<>();
        response.setStatus(false);
        response.setError_code("AB_1001");
        response.setMessage(ex.getMessage());
        response.setMessage(ex.toString());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationError(MethodArgumentNotValidException ex) {

        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        ApiResponse<String> response = new ApiResponse<>();

        response.setStatus(false);
        response.setError_code(ErrorCodeEnum.getCode(errorMessage));
        response.setMessage(errorMessage);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiResponse<?>> handleAbdmError(WebClientResponseException ex) {

        String errorCode = "";
        String errorMessage = "";

        try {
            JsonNode root = new ObjectMapper().readTree(ex.getResponseBodyAsString());
            errorCode = root.path("error").path("code").asText(errorCode);
            errorMessage = root.path("error").path("message").asText(errorMessage);
        } catch (Exception ignored) {}

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(ResponseUtils.createFailureResponse(errorMessage, false, errorCode));
    }


}
