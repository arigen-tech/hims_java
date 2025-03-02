package com.hims.dto;

import com.hims.entity.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.time.Instant;

/**
 * DTO for {@link com.hims.entity.Visit}
 */
public record VisitDto(Integer id, @NotNull Long tokenNo, Instant visitDate, Long priority, Long departmentId,
                       UserDto doctor, @Size(max = 100) String doctorName, @NotNull PatientDto patient,
                       @NotNull MasHospitalDto hospital, User iniDoctor, MasOpdSessionDto session,
                       @NotNull @Size(max = 1) String billingStatus, String visitStatus) implements Serializable {
}
