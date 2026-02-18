package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasBloodInventoryStatusRequest {

    @NotBlank(message = "Status code is required")
    @Size(max = 20, message = "Status code max length is 20")
    private String statusCode;

    @NotBlank(message = "Description is required")
    @Size(max = 300, message = "Description max length is 300")
    private String description;
}
