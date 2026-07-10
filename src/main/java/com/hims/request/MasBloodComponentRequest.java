package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasBloodComponentRequest {

    @NotBlank
    private String componentCode;

    @NotBlank
    private String componentName;

    private String description;
    private String storageTemp;
    private Integer shelfLifeDays;
}