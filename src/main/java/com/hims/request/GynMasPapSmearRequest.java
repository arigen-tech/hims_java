package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GynMasPapSmearRequest {

    @NotBlank
    private String papResult;
}
