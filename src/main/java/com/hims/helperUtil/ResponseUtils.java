package com.hims.helperUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.response.ApiResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

@UtilityClass

public class ResponseUtils {

    public <T> ApiResponse<T> createSuccessResponse(T data, TypeReference<T> tClass) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setResponse(data);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("success");
        return response;
    }

    public <T> ApiResponse<T> createSuccessResponseWithCred(T data, TypeReference<T> tClass) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setResponse(data);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("success");
        response.setKey("4gcXBD");
        response.setSalt("uEUTeQqzQhwwHg0qZCpYVRA9ufPnR9zE");
        response.setProduction(true);
        return response;
    }

    public <T> ApiResponse<T> createFailureResponse(T data, TypeReference<T> tClass, String msg, Integer status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setResponse(data);
        response.setStatus(status);
        response.setMessage(msg);
        return response;
    }

    public <T> ApiResponse<T> createNotFoundResponse(String msg, Integer status) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(status);
        response.setMessage(msg);
        return response;
    }

    public static String getReturnMsg(String status, String message) {
        return "{\"status\": \"" + status + "\", \"message\": \"" + message + "\"}";
    }


}
