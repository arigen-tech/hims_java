package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
public class PreviousPaymentHistoryResponse {
    private Long receiptId;
    private LocalDateTime dateTime;
    private Long paymentTymeId;
    private String paymentType;
    private Long paymentModeId;
    private String paymentMode;
    private BigDecimal amount;
}
