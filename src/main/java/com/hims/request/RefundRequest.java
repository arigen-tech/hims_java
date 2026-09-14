package com.hims.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class RefundRequest {

    @NotNull
    private Long billingHeaderId;

    @NotNull
    @Positive
    private BigDecimal refundAmount;

    private  Long refundReasonId;
}