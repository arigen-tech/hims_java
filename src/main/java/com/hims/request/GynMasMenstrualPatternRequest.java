package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GynMasMenstrualPatternRequest {

    @NotBlank
    private String patternValue;
}
