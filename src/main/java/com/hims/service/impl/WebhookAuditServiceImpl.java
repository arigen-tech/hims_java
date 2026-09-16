package com.hims.service.impl;

import com.hims.constants.AppConstants;
import com.hims.entity.PaymentDetailsV2;
import com.hims.entity.PaymentWebhookEvent;
import com.hims.entity.repository.PaymentWebhookEventRepository;
import com.hims.exception.SDDException;
import com.hims.service.WebhookAuditService;
import com.hims.utils.HMISUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WebhookAuditServiceImpl implements WebhookAuditService{



    private final PaymentWebhookEventRepository webhookEventRepository;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentWebhookEvent saveWebhookEvent(
            String eventId,
            String eventType,
            String gatewayOrderId,
            String gatewayPaymentId,
            String rawBody,
            Optional<PaymentDetailsV2> paymentOpt) {

        PaymentWebhookEvent webhookEvent = new PaymentWebhookEvent();

        webhookEvent.setEventId(eventId);
        webhookEvent.setEventType(eventType);
        webhookEvent.setGateway(AppConstants.RAZORPAY_GATEWAY);
        webhookEvent.setGatewayOrderId(gatewayOrderId);
        webhookEvent.setGatewayPaymentId(gatewayPaymentId);
        webhookEvent.setPayload(rawBody);
        webhookEvent.setProcessingStatus(AppConstants.WEBHOOK_RECEIVED_STATUS);
        paymentOpt.ifPresent(webhookEvent::setPayment);
        webhookEvent.setReceivedAt(HMISUtil.getCurrentLocalDateTime());
        webhookEvent.setCreatedAt(HMISUtil.getCurrentLocalDateTime());
        webhookEvent.setUpdatedAt(HMISUtil.getCurrentLocalDateTime());

        return webhookEventRepository.save(webhookEvent);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markProcessed(Long webhookEventId) {

        PaymentWebhookEvent event =
                webhookEventRepository
                        .findById(webhookEventId)
                        .orElseThrow(
                                () -> new SDDException(
                                        "Webhook",
                                        HttpStatus.NOT_FOUND.value(),
                                        "Webhook event not found: " + webhookEventId
                                )
                        );

        event.setProcessingStatus(AppConstants.WEBHOOK_PROCESSED_STATUS);
        event.setProcessedAt(HMISUtil.getCurrentLocalDateTime());
        event.setUpdatedAt(HMISUtil.getCurrentLocalDateTime());

        webhookEventRepository.save(event);
    }


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long webhookEventId, String errorMessage) {

        PaymentWebhookEvent event =
                webhookEventRepository
                        .findById(webhookEventId)
                        .orElseThrow(
                                () -> new SDDException(
                                        "Webhook",
                                        HttpStatus.NOT_FOUND.value(),
                                        "Webhook event not found: " + webhookEventId
                                )
                        );

        event.setProcessingStatus(AppConstants.WEBHOOK_FAILED_STATUS);
        event.setErrorMessage(truncate(errorMessage, 2000));
        event.setUpdatedAt(HMISUtil.getCurrentLocalDateTime());

        webhookEventRepository.save(event);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentWebhookEvent saveWebhookEvent(
            String eventId, String eventType, String gatewayOrderId, String gatewayPaymentId,
            String rawBody, List<PaymentDetailsV2> matchedPayments) {

        PaymentWebhookEvent webhookEvent = new PaymentWebhookEvent();
        webhookEvent.setEventId(eventId);
        webhookEvent.setEventType(eventType);
        webhookEvent.setGateway(AppConstants.RAZORPAY_GATEWAY);
        webhookEvent.setGatewayOrderId(gatewayOrderId);
        webhookEvent.setGatewayPaymentId(gatewayPaymentId);
        webhookEvent.setPayload(rawBody);
        webhookEvent.setProcessingStatus(AppConstants.WEBHOOK_RECEIVED_STATUS);

        webhookEvent.setPayments(matchedPayments);
        matchedPayments.stream().findFirst().ifPresent(webhookEvent::setPayment);

        webhookEvent.setReceivedAt(HMISUtil.getCurrentLocalDateTime());
        webhookEvent.setCreatedAt(HMISUtil.getCurrentLocalDateTime());
        webhookEvent.setUpdatedAt(HMISUtil.getCurrentLocalDateTime());

        return webhookEventRepository.save(webhookEvent);
    }


    private String truncate(String value, int maxLength) {

        if (value == null) {
            return null;
        }

        return value.length() > maxLength
                ? value.substring(0, maxLength)
                : value;
    }
}