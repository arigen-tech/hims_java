package com.hims.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentGatewayStatusResponse {

        private Long paymentId;

        private String paymentStatus;

        private String gatewayOrderId;

        private String gatewayPaymentId;

        private BigDecimal amount;

        private String currency;

}
