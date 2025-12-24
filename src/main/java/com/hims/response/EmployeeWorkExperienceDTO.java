package com.hims.response;

import com.hims.entity.EmployeeWorkExperience;
import lombok.Builder;

import java.time.Instant;

@Builder
public record EmployeeWorkExperienceDTO(
        Long experienceId,
        String experienceSummary,
        Instant lastUpdateDate,
        Integer orderLevel
) {

    public static EmployeeWorkExperienceDTO fromEntity(
            EmployeeWorkExperience employeeWorkExperience
    ) {
        return EmployeeWorkExperienceDTO.builder()
                .experienceId(employeeWorkExperience.getExperienceId())
                .experienceSummary(employeeWorkExperience.getExperienceSummary())
                .lastUpdateDate(employeeWorkExperience.getLastUpdateDate())
                .orderLevel(employeeWorkExperience.getOrderLevel())
                .build();
    }
}
