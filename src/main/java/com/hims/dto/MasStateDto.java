package com.hims.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link com.hims.entity.MasState}
 */
public record MasStateDto(Long id, @Size(max = 8) String stateCode,
                          @Size(max = 30) String stateName) implements Serializable {
}
