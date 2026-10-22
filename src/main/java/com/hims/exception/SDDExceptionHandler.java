package com.hims.exception;



import com.hims.response.ApiResponse;
import com.hims.request.DietOrderRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
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

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        if (ex.getBindingResult().getTarget() instanceof DietOrderRequest) {
            String message = ex.getBindingResult().getFieldErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .findFirst()
                    .orElse("Invalid diet order request");
            return ResponseEntity.badRequest().body(message);
        }

        return super.handleMethodArgumentNotValid(ex, headers, status, request);
    }


}
