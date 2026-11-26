package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtSurgeryExecutionResponse {
    private Long otBookingId;
    private Long otSurgeryExecutionId;
    private Long otBookingRequestDtId;
    private LocalDateTime surgeryStartDatetime;
    private LocalDateTime surgeryEndDatetime;
    private String executionStatus;
    private String procedureConfirmed;
    private String instrumentCountCorrect;
    private String specimenLabelVerified;
    private String equipmentIssuesReviewed;
    private String recoveryConcernsReviewed;
    private String signoutRemarks;
    private String signoutVerifiedBy;
    private LocalDateTime signoutVerifiedDatetime;
    private String remarks;
    private String status;
    private String lastChgBy;
    private LocalDateTime lastChgDate;
}
