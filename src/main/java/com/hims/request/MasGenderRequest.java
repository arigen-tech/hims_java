package com.hims.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasGenderRequest {
    @NotNull
    @Size(max = 1)
    private String genderCode;

    @NotNull
    private String genderName;

    @Size(max = 5)
    private String code;

    private String status;
}