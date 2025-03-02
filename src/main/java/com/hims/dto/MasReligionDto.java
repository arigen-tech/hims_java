package com.hims.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link com.hims.entity.MasReligion}
 */
public record MasReligionDto(Integer id, @Size(max = 50) String name, String status) implements Serializable {
}
