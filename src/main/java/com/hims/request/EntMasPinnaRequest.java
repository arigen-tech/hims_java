package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EntMasPinnaRequest {

    @NotBlank(message = "Pinna status is required")
    private String pinnaStatus;
}
