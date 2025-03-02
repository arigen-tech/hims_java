package com.hims.dto;

import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO for {@link com.hims.entity.MasMaritalStatus}
 */
public record MasMaritalStatusDto(Integer id, @Size(max = 30) String name, String status) implements Serializable {
}
