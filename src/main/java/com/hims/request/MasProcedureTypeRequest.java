package com.hims.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MasProcedureTypeRequest {
    private String procedureTypeName;
    private String description;
    private String procedureTypeCode;
    @NotNull(message = "Department ID is required")
    private Long departmentId;
}
