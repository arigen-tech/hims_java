package com.hims.response;

import com.hims.entity.EmployeeMembership;
import lombok.Builder;

import java.time.Instant;

@Builder
public record EmployeeMembershipDTO (
    Long membershipId,
    String membershipSummary,
    Instant lastUpdateDate,
    Integer orderLevel
){
    public static EmployeeMembershipDTO fromEntity(EmployeeMembership employeeMembership) {
        return EmployeeMembershipDTO.builder()
                .membershipId(employeeMembership.getMembershipId())
                .membershipSummary(employeeMembership.getMembershipSummary())
                .lastUpdateDate(employeeMembership.getLastUpdateDate())
                .orderLevel(employeeMembership.getOrderLevel())
                .build();
    }
}
