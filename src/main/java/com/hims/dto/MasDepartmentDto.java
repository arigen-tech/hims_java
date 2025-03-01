package com.hims.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link com.hims.entity.MasDepartment}
 */
public record MasDepartmentDto(Long id, @Size(max = 350) String departmentCode, @Size(max = 350) String departmentName,
                               MasDepartmentTypeDto departmentType, MasHospitalDto hospital,
                               @Size(max = 50) String departmentNo) implements Serializable {
}
