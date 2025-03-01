package com.hims.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link com.hims.entity.MasGender}
 */
public record MasGenderDto(Long id, @NotNull @Size(max = 1) String genderCode, @NotNull String genderName,
                           @Size(max = 5) String code) implements Serializable {
}
