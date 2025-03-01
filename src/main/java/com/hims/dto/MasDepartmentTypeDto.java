package com.hims.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link com.hims.entity.MasDepartmentType}
 */
public record MasDepartmentTypeDto(Long id, @Size(max = 8) String departmentTypeCode,
                                   @Size(max = 30) String departmentTypeName) implements Serializable {
}
