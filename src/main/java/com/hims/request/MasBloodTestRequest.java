package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasBloodTestRequest {

    @NotBlank(message = "Test code is required")
    @Size(max = 30)
    private String testCode;

    @NotBlank(message = "Test name is required")
    @Size(max = 100)
    private String testName;
    private String isMandatory;
    private Long applicableCollectionTypeId;
}
