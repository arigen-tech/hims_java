package com.hims.response;

import com.hims.entity.EmployeeSpecialtyInterest;
import lombok.Builder;

import java.time.Instant;

@Builder
public record EmployeeSpecialtyInterestDTO(
        Long interestId,
        String interestSummary,
        Instant lastUpdateDate
) {

    public static EmployeeSpecialtyInterestDTO fromEntity(
            EmployeeSpecialtyInterest employeeSpecialtyInterest
    ) {
        return EmployeeSpecialtyInterestDTO.builder()
                .interestId(employeeSpecialtyInterest.getInterestId())
                .interestSummary(employeeSpecialtyInterest.getInterestSummary())
                .lastUpdateDate(employeeSpecialtyInterest.getLastUpdateDate())
                .build();
    }
}
