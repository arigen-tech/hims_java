package com.hims.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link com.hims.entity.MasDistrict}
 */
public record MasDistrictDto(Long id, @NotNull @Size(max = 100) String districtName, String status) implements Serializable {
}
