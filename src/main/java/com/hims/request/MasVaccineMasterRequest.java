package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MasVaccineMasterRequest {

    @NotBlank(message = "Vaccine label is required")
    private String vaccineLabel;

    @NotBlank(message = "Recommended age is required")
    private String recommendedAge;

    @NotBlank(message = "Vaccine group is required")
    private String vaccineGroup;

    @NotNull(message = "Display order is required")
    private Integer displayOrder;

    private String isMultiDose;

    private Integer dosePerVial;
}
