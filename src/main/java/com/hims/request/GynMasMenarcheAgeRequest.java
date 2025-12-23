package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GynMasMenarcheAgeRequest {

    @NotBlank
    private String menarcheAge;
}
