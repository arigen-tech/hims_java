package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EntMasWeberRequest {

    @NotBlank(message = "Weber result is required")
    private String weberResult;
}
