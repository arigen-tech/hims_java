package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MasOtBookingStatusRequest {

    @NotBlank(message = "Status code is required")
    @Size(max = 30, message = "Status code must not exceed 30 characters")
    private String statusCode;

    @NotBlank(message = "Status name is required")
    @Size(max = 100, message = "Status name must not exceed 100 characters")
    private String statusName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}