package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabOrderTrackingStatusRequest {

    @NotBlank(message = "Order status code is required")
    @Size(max = 10, message = "Order status code must not exceed 10 characters")
    private String orderStatusCode;

    @NotBlank(message = "Order status name is required")
    @Size(max = 100, message = "Order status name must not exceed 100 characters")
    private String orderStatusName;


    @Size(max = 500, message = "Order status description must not exceed 500 characters")
    private String description;
}
