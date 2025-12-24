package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EntMasMucosaRequest {

    @NotBlank(message = "Mucosa status is required")
    private String mucosaStatus;
}
