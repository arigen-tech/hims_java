package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasLabResultAmendmentTypeRequest {

    @NotBlank(message = "Amendment code is required")
    @Size(max = 30, message = "Amendment code must not exceed 30 characters")
    private String amendmentTypeCode;

    @NotBlank(message = "Amendment name is required")
    @Size(max = 100, message = "Amendment name must not exceed 100 characters")
    private String amendmentTypeName;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
}

