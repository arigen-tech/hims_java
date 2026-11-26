package com.hims.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OtSurgeryAnaesthesiaRequest {
    @NotNull @Positive
    private Long otBookingId;
    @NotNull @Positive
    private Long otBookingRequestDtId;

    private LocalDateTime surgeryStartDatetime;
    private LocalDateTime surgeryEndDatetime;
    private String executionStatus;
    private String signoutRemarks;
    private String signoutVerifiedBy;
    private LocalDateTime signoutVerifiedDatetime;
    private String executionRemarks;

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
    private String lastChgBy;
}
