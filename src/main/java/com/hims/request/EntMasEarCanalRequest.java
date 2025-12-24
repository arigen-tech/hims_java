package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EntMasEarCanalRequest {

    @NotBlank(message = "Ear canal condition is required")
    private String earCanalCondition;
}
