package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EntMasSeptumRequest {

    @NotBlank(message = "Septum status is required")
    private String septumStatus;
}
