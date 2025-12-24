package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EntMasRinneRequest {

    @NotBlank(message = "Rinne result is required")
    private String rinneResult;
}
