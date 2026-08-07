package com.hims.exception;



import com.hims.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Slf4j
public class SDDExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(SDDException.class)
    public ResponseEntity<ApiResponse<String>> handleSDDException(SDDException sddException) {
        ApiResponse<String> errorApiResponse = new ApiResponse<>();
        errorApiResponse.setMessage(sddException.getMessage());
        errorApiResponse.setStatus(sddException.getStatus());
        errorApiResponse.setResponse(sddException.getMessage());
        return ResponseEntity.status(HttpStatus.valueOf(sddException.getStatus())).body(errorApiResponse);
    }


}
