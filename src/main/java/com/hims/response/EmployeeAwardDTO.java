package com.hims.response;

import com.hims.entity.EmployeeAward;
import lombok.Builder;

import java.time.Instant;

@Builder
public record EmployeeAwardDTO(
        Long awardId,
        String awardSummary,
        Instant lastUpdateDate
) {

    public static EmployeeAwardDTO fromEntity(EmployeeAward award) {
        return EmployeeAwardDTO.builder()
                .awardId(award.getAwardId())
                .awardSummary(award.getAwardSummary())
                .lastUpdateDate(award.getLastUpdateDate())
                .build();
    }
}
