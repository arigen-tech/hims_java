package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientBillingRefundDetailsResponse {

    private String refundStatus;
    private BigDecimal refundAmount;
    private String refundMode;
    private String transactionNumber;
    private LocalDateTime refundDate;
    private String processedBy;
}