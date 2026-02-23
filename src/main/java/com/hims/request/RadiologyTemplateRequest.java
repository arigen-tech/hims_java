package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RadiologyTemplateRequest {
    @NotBlank(message = "templateCode is required")
    private String templateCode;
    @NotBlank(message = "templateName is required")
    private String templateName;
    @NotNull(message = "subChargecodeId is required")
    @Positive(message = "subChargecodeId must be positive")
    private Long subChargecodeId;

    @NotBlank(message = "templateText is required")
    private String templateText;
}
