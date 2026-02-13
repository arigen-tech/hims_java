package com.hims.exception;

import com.fasterxml.jackson.core.type.TypeReference;
import com.hims.exception.bloodBankException.DonorSaveException;
import com.hims.exception.bloodBankException.ScreeningSaveException;
import com.hims.exception.patientRegistrationException.AppSetupNotFoundException;
import com.hims.exception.patientRegistrationException.TokenAlreadyBookedException;
import com.hims.response.ApiResponse;
import com.hims.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BillingException.class)
    public ResponseEntity<ApiResponse<?>> handleBillingException(BillingException ex) {

        ApiResponse<Object> resp = new ApiResponse<>();
        resp.setResponse(null);
        resp.setStatus(400);
        resp.setMessage(ex.getMessage());
        resp.setSalt(null);
        resp.setProduction(false);
        resp.setKey(null);

        return new ResponseEntity<>(resp, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PrinterNotFoundException.class)
    public ResponseEntity<?> handlePrinterNotFound(PrinterNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ResponseUtils.createNotFoundResponse(
                        ex.getMessage(),
                        503
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseUtils.createNotFoundResponse(
                        "Internal server error",
                        500
                ));
    }

    @ExceptionHandler(DonorSaveException.class)
    public ResponseEntity<ApiResponse<Object>> handleDonorError(DonorSaveException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        ex.getMessage(), 400));
    }

    @ExceptionHandler(ScreeningSaveException.class)
    public ResponseEntity<ApiResponse<Object>> handleScreeningError(ScreeningSaveException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResponseUtils.createFailureResponse(
                        null, new TypeReference<>() {},
                        ex.getMessage(), 400));
    }


    @ExceptionHandler(TokenAlreadyBookedException.class)
    public ResponseEntity<ApiResponse<Object>> handleTokenException(
            TokenAlreadyBookedException ex) {

        ApiResponse<Object> response =
                ResponseUtils.createFailureResponse(
                        null,
                        new TypeReference<Object>() {},
                        ex.getMessage(),
                        HttpStatus.CONFLICT.value()
                );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AppSetupNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppSetup(
            AppSetupNotFoundException ex) {

        ApiResponse<Object> response =
                ResponseUtils.createFailureResponse(
                        null,
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST.value()
                );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
