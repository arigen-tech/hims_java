package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EntMasTonsilGradeRequest {

    @NotBlank(message = "Tonsil grade is required")
    private String tonsilGrade;
}
