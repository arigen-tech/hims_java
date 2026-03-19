package com.hims.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasQuestionOptionValueRequest {

    @NotBlank(message = "Option code is required")
    @Size(max = 8, message = "Option code must not exceed 8 characters")
    private String optionCode;

    @NotBlank(message = "Option value is required")
    @Size(max = 1000, message = "Option value must not exceed 1000 characters")
    private String optionValue;

    @NotNull(message = "Option score is required")
    private Integer optionScore;

    @NotNull(message = "Question id is required")
    private Long questionId;
}