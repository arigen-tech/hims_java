package com.hims.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MasCommonStatusRequest {

    @NotBlank(message = "Entity name is required")
    @Size(max = 100, message = "Entity name must not exceed 100 characters")
    private String entityName;

    @NotBlank(message = "Table name is required")
    @Size(max = 100, message = "Table name must not exceed 100 characters")
    private String tableName;


    @NotBlank(message = "Column name is required")
    @Size(max = 100, message = "Column name must not exceed 100 characters")
    private String columnName;

    @NotBlank(message = "Status code is required")
    @Size(max = 10, message = "Status code must not exceed 10 characters")
    private String statusCode;

    @NotBlank(message = "Status name is required")
    @Size(max = 50, message = "Status name must not exceed 50 characters")
    private String statusName;

    @NotBlank(message = "Status description is required")
    private String statusDesc;

    @Size(max = 100, message = "Remarks must not exceed 100 characters")
    private String remarks;
}
