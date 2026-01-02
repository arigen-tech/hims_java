package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasPatientPreparationRequest {

    @NotBlank(message = "Preparation code is required")
    @Size(max = 30, message = "Preparation code must not exceed 30 characters")
    private String preparationCode;

    @NotBlank(message = "Preparation name is required")
    @Size(max = 150, message = "Preparation name must not exceed 150 characters")
    private String preparationName;

    @NotBlank(message = "Instructions are required")
    private String instructions;

    @NotBlank(message = "Applicable To is required")
    private String applicableTo;
}
