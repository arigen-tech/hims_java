package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MasQuestionRequest {

    @NotBlank
    @Size(max = 500)
    private String question;

    @NotNull
    private Long questionHeadingId;

    private Integer optionValue;
}
