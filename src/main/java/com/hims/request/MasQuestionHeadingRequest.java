package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MasQuestionHeadingRequest {

    @NotBlank
    @Size(max = 8)
    private String questionHeadingCode;

    @NotBlank
    @Size(max = 250)
    private String questionHeadingName;
}
