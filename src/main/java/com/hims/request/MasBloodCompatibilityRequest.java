package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasBloodCompatibilityRequest {

    @NotNull
    private Long componentId;

    @NotNull
    private Long patientBloodGroupId;

    @NotNull
    private Long donorBloodGroupId;

    @NotBlank
    private String isPreferred;
}
