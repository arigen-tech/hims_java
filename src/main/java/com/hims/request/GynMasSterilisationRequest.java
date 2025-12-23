package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GynMasSterilisationRequest {

    @NotBlank
    private String sterilisationType;
}
