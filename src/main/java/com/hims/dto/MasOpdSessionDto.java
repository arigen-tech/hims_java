package com.hims.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * DTO for {@link com.hims.entity.MasOpdSession}
 */
public record MasOpdSessionDto(Long id, @NotNull @Size(max = 255) String sessionName, @NotNull LocalTime fromTime,
                               @NotNull LocalTime endTime,
                               @NotNull @Size(max = 50) String status) implements Serializable {
}
