package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasBloodCollectionTypeRequest {

    @NotBlank(message = "Collection type code is required")
    @Size(max = 10, message = "Code length must be <= 10")
    private String collectionTypeCode;

    @NotBlank(message = "Collection type name is required")
    @Size(max = 50, message = "Name length must be <= 50")
    private String collectionTypeName;

    @Size(max = 300, message = "Description length must be <= 300")
    private String description;
}
