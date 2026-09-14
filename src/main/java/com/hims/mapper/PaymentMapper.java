package com.hims.mapper;

import com.hims.projection.RefundDetailsProjection;
import com.hims.response.RefundDetailsResponse;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public RefundDetailsResponse getRefundDetails(RefundDetailsProjection projection) {



        return RefundDetailsResponse.builder()
                .refundId(projection.getRefundId())
                .gatewayRefundId(projection.getGatewayRefundId())
                .refundNumber(projection.getRefundNumber())
                .refundAmount(projection.getRefundAmount())
                .paymentAmount(projection.getPaymentAmount())
                .refundStatus(projection.getRefundStatus())
                .refundReason(projection.getRefundReason())
                .initiatedOn(projection.getInitiatedOn())
                .gatewayPaymentId(projection.getGatewayPaymentId())
                .paymentMode(projection.getPaymentMode())
                .paymentVia(projection.getPaymentVia())
                .gatewayReferenceType(projection.getGatewayReferenceType())
                .gatewayReferenceNo(projection.getGatewayReferenceNo())
                .build();
    }
}
