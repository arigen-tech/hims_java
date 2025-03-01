package com.hims.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link com.hims.entity.MasCountry}
 */
public record MasCountryDto(Long id, @Size(max = 8) String countryCode,
                            @Size(max = 30) String countryName) implements Serializable {
}
