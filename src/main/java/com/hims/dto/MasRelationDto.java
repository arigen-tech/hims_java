package com.hims.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link com.hims.entity.MasRelation}
 */
public record MasRelationDto(Long id, @NotNull @Size(max = 30) String relationName, Instant lastChgDate,
                             @Size(max = 5) String code) implements Serializable {
}
