package com.hims.service;

import com.hims.entity.PaymentDetailsV2;
import com.hims.entity.PaymentWebhookEvent;

import java.util.List;
import java.util.Optional;

public interface WebhookAuditService {

     PaymentWebhookEvent saveWebhookEvent(
            String eventId,
            String eventType,
            String gatewayOrderId,
            String gatewayPaymentId,
            String rawBody,
            Optional<PaymentDetailsV2> paymentOpt);

     void markProcessed(Long webhookEventId);

     void markFailed(Long webhookEventId, String errorMessage);

     PaymentWebhookEvent saveWebhookEvent(
             String eventId, String eventType, String gatewayOrderId, String gatewayPaymentId,
             String rawBody, List<PaymentDetailsV2> matchedPayments);
}
