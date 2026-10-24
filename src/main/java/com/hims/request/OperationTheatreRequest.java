package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationTheatreRequest {

    @NotBlank(message = "OT code is required")
    @Size(max = 20, message = "OT code must not exceed 20 characters")
    private String otCode;

    @NotBlank(message = "OT name is required")
    @Size(max = 100, message = "OT name must not exceed 100 characters")
    private String otName;

    @NotNull(message = "OT type ID is required")
    private Long otTypeId;

    private String otType;

    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;
}