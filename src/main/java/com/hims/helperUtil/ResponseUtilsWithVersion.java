package com.hims.helperUtil;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.response.ApiResponseWithVersion;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

@UtilityClass

public class ResponseUtilsWithVersion {

    public <T> ApiResponseWithVersion<T> createSuccessResponse(T data, TypeReference<T> tClass) {
        ApiResponseWithVersion<T> response = new ApiResponseWithVersion<>();
        response.setResponse(data);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("success");
        response.setAndroidVersion("prod_v1.1.0");
        response.setApiVersion("prod_v1.1.0");
        response.setIosVersion("prod_v1.1.0");
        return response;
    }
}
