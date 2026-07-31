package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasBloodUnitStatusRequest {

    @NotBlank(message = "Status code is required")
    @Size(max = 10, message = "Status code max 10 characters")
    private String statusCode;

    @NotBlank(message = "Status name is required")
    @Size(max = 50, message = "Status name max 50 characters")
    private String statusName;

    @Size(max = 200, message = "Description max 200 characters")
    private String description;
}
