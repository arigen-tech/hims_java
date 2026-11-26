package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** API representation of the surgery execution and its anaesthesia details. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtSurgeryAnaesthesiaResponse {
    private Long otBookingId;
    private Execution execution;
    private Anaesthesia anaesthesia;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Execution {
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

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Anaesthesia {
        private Long otSurgeryAnaesthesiaId;
        private Long otSurgeryExecutionId;
        private Long anaesthesiaTypeId;
        private Long anaesthetistId;
        private Long assistantAnaesthetistId;
        private LocalDateTime anaesthesiaStartDatetime;
        private LocalDateTime anaesthesiaEndDatetime;
        private String airwayManagement;
        private Long asaGradeId;
        private String preInductionBp;
        private Integer preInductionPulse;
        private BigDecimal preInductionSpo2;
        private String anaesthesiaNotes;
        private String complication;
        private String complicationRemarks;
        private String status;
        private String lastChgBy;
        private LocalDateTime lastChgDate;
    }
}
