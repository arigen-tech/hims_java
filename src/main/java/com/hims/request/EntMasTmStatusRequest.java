package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EntMasTmStatusRequest {

    @NotBlank(message = "TM status is required")
    private String tmStatus;
}

