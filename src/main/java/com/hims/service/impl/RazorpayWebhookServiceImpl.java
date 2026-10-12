package com.hims.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hims.constants.PaymentStatusCode;
import com.hims.entity.PaymentDetailsV2;
import com.hims.entity.PaymentRefund;
import com.hims.entity.PaymentWebhookEvent;
import com.hims.entity.repository.PaymentDetailsV2Repository;
import com.hims.entity.repository.PaymentRefundRepository;
import com.hims.entity.repository.PaymentWebhookEventRepository;
import com.hims.exception.SDDException;
import com.hims.service.RazorpayWebhookService;
import com.hims.service.TransactionSequenceService;
import com.hims.service.WebhookAuditService;
import com.hims.utils.AuthUtil;
import com.hims.utils.HMISTransaction;
import com.hims.utils.HMISUtil;
import com.hims.utils.PaymentUtils;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayWebhookServiceImpl
        implements RazorpayWebhookService {

    private static final String RAZORPAY = "RAZORPAY";

    private final PaymentDetailsV2Repository paymentRepository;

    private final PaymentWebhookEventRepository webhookEventRepository;

    private final PaymentRefundRepository refundRepository;

    private final PaymentUtils paymentUtils;

    private final WebhookAuditService webhookAuditService;

    private final TransactionSequenceService transactionSequenceService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AuthUtil authUtil;


    /**
     * ==========================================================
     * MAIN WEBHOOK METHOD
     * ==========================================================
     *
     * Flow:
     *
     * 1. Verify Razorpay signature
     * 2. Parse webhook JSON
     * 3. Get event ID/type
     * 4. Check duplicate event
     * 5. Extract order/payment/refund IDs
     * 6. Resolve HMIS payment
     * 7. Save webhook event
     * 8. Process business logic
     * 9. Mark webhook PROCESSED
     * 10. If processing fails, mark webhook FAILED
     */

    @Override
    public void processWebhook( String rawBody,
                                String razorpaySignature,
                                String eventIdHeader,
                                String webhookSecret
    ) {
        try {
            verifyWebhookSignature(rawBody, razorpaySignature, webhookSecret);
        } catch (Exception e) {
            throw e;
        }
        JsonNode root;

        try {

            root = objectMapper.readTree(rawBody);

        } catch (Exception e) {
            throw new IllegalArgumentException("Malformed webhook JSON body", e);
        }
        String eventId = eventIdHeader;
        String eventType = textOrNull(root, "event");
        if (eventId == null || eventId.isBlank() || eventType == null){
            throw new IllegalArgumentException(
                    "Webhook missing X-Razorpay-Event-Id header or 'event' field"
            );
        }
        boolean eventAlreadyExists =
                webhookEventRepository
                        .existsByGatewayAndEventId(
                                RAZORPAY,
                                eventId
                        );
        if (eventAlreadyExists) {
            return;
        }
        JsonNode paymentEntity =
                root.at("/payload/payment/entity");

        JsonNode orderEntity =
                root.at("/payload/order/entity");

        JsonNode refundEntity =
                root.at("/payload/refund/entity");
        String gatewayOrderId =
                firstNonNull(
                        textOrNull(
                                paymentEntity,
                                "order_id"
                        ),
                        textOrNull(
                                orderEntity,
                                "id"
                        )
                );
        String gatewayPaymentId = firstNonNull(
                textOrNull(paymentEntity, "id"),
                textOrNull(refundEntity, "payment_id")
        );

        /*
         * One gatewayOrderId can now map to MULTIPLE rows -- one per
         * billing header paid together in a single multi-appointment
         * checkout. See resolvePayment().
         */
        List<PaymentDetailsV2> payments = resolvePayment(gatewayOrderId, gatewayPaymentId);
        List<PaymentDetailsV2> auditPayments = resolveAuditPayments(refundEntity, payments);

        PaymentWebhookEvent webhookEvent = webhookAuditService.saveWebhookEvent(
                eventId, eventType, gatewayOrderId, gatewayPaymentId, rawBody, auditPayments);


        /*
         * Audit log only needs ONE representative row to link to
         * (payment_webhook_event.payment_id is a single FK) -- take
         * the first match from the group, if any.
         */
//        PaymentWebhookEvent webhookEvent =
//                webhookAuditService.saveWebhookEvent(
//                        eventId,
//                        eventType,
//                        gatewayOrderId,
//                        gatewayPaymentId,
//                        rawBody,
//                        auditPayment
//                );
        try {

            processWebhookBusinessLogic(
                    eventType,
                    payments,
                    paymentEntity,
                    refundEntity
            );

            webhookAuditService.markProcessed(
                    webhookEvent.getWebhookEventId()
            );
        } catch (Exception e) {
            try {

                webhookAuditService.markFailed(
                        webhookEvent.getWebhookEventId(),
                        e.getMessage()
                );

            } catch (Exception auditException) {
                log.error("processWebhook method error :: ",e);
//                throw e;
            }
        }
    }

    @Transactional
    public void processWebhookBusinessLogic(
            String eventType,
            List<PaymentDetailsV2> payments,
            JsonNode paymentEntity,
            JsonNode refundEntity) {

        switch (eventType) {
            case "payment.captured":

                handlePaymentCaptured(
                        payments,
                        paymentEntity
                );

                break;
            case "payment.failed":

                handlePaymentFailed(
                        payments,
                        paymentEntity
                );

                break;
            case "order.paid":

                log.info(
                        "order.paid received. " +
                                "Payment status handled by payment.captured."
                );

                break;
            case "refund.created":

                handleRefundCreated(
                        refundEntity
                );

                break;
            case "refund.processed":
                handleRefundProcessed(
                        refundEntity
                );

                break;
            default:

                log.info(
                        "Unhandled Razorpay event type: {}",
                        eventType
                );
        }
    }


    /**
     * ==========================================================
     * VERIFY RAZORPAY WEBHOOK SIGNATURE
     * ==========================================================
     */
    private void verifyWebhookSignature(String rawBody, String razorpaySignature, String webhookSecret) {

        log.info("--------------------------------------------------");
        log.info("RAZORPAY WEBHOOK SIGNATURE VERIFICATION STARTED");
        log.info("--------------------------------------------------");

        log.info("Signature Step 1: Verifying webhook request body");

        log.info("Signature Step 1.1: Webhook body present: {}", rawBody != null && !rawBody.isBlank());

        if (rawBody == null || rawBody.isBlank()) {

            log.error("Signature Step 1.2: Webhook body is missing or empty");
            log.error("Signature Step 1.3: Rejecting webhook because request body is invalid");

            throw new SDDException(
                    "Web hook",
                    HttpStatus.NOT_FOUND.value(),
                    "Missing webhook body"
            );
        }

        log.info("Signature Step 1.2: Webhook body validation successful .Payload length={}", rawBody.length());


        /*
         * ======================================================
         * 2. VERIFY RAZORPAY SIGNATURE HEADER
         * ======================================================
         */

        log.info("Signature Step 2: Checking Razorpay signature header");

        log.info("Signature Step 2.1: Razorpay signature present: {}",
                razorpaySignature != null && !razorpaySignature.isBlank()
        );

        if (razorpaySignature == null || razorpaySignature.isBlank()) {

            log.error("Signature Step 2.2: Razorpay webhook signature is missing");
            log.error("Signature Step 2.3: Rejecting webhook because signature header is missing");

            throw new SecurityException("Missing Razorpay webhook signature");
        }

        log.info("Signature Step 2.2: Razorpay signature header received successfully");


        /*
         * ======================================================
         * 3. VERIFY WEBHOOK SECRET
         * ======================================================
         */

        log.info("Signature Step 3: Checking Razorpay webhook secret configuration");

        boolean webhookSecretConfigured = webhookSecret != null && !webhookSecret.isBlank();

        log.info("Signature Step 3.1: Webhook secret configured: {}", webhookSecretConfigured);

        if (!webhookSecretConfigured) {

            log.error("Signature Step 3.2: Razorpay webhook secret is NOT configured");
            log.error("Signature Step 3.3: Rejecting webhook because webhook secret is missing");

            throw new SecurityException("Razorpay webhook secret is not configured");
        }

        log.info("Signature Step 3.2: Razorpay webhook secret is configured");
        /*
         * ======================================================
         * 4. VERIFY SIGNATURE USING RAZORPAY UTILITY
         * ======================================================
         */

        log.info("Signature Step 4: Starting Razorpay signature verification");

        boolean signatureValid;

        try {

            log.info("Signature Step 4.1: Calling Utils.verifyWebhookSignature()");

            signatureValid = Utils.verifyWebhookSignature(
                    rawBody,
                    razorpaySignature,
                    webhookSecret
            );

            log.info("Signature Step 4.2: Utils.verifyWebhookSignature() completed");
            log.info("Signature Step 4.3: Signature validation result={}", signatureValid);

        } catch (Exception e) {

            log.error("Signature Step 4.4: Exception occurred during Razorpay signature verification");
            log.error("Signature Step 4.5: Exception type={}", e.getClass().getName());
            log.error("Signature Step 4.6: Exception message={}", e.getMessage());
            log.error("Signature Step 4.7: Full signature verification exception", e);

            throw new SecurityException("Invalid webhook signature", e);
        }


        /*
         * ======================================================
         * 5. CHECK VERIFICATION RESULT
         * ======================================================
         */

        log.info("Signature Step 5: Checking signature verification result");

        if (!signatureValid) {
            log.warn("Signature Step 5.1: Razorpay webhook signature is INVALID");
            log.warn("Signature Step 5.2: Webhook request will be rejected");
            log.warn("Signature Step 5.3: Possible reasons: incorrect webhook secret, " +
                            "modified request body, or incorrect signature"
            );
            log.warn("--------------------------------------------------");
            log.warn("RAZORPAY WEBHOOK SIGNATURE VERIFICATION FAILED");
            log.warn("--------------------------------------------------");

            throw new SecurityException("Invalid webhook signature");
        }


        /*
         * ======================================================
         * 6. SUCCESS
         * ======================================================
         */

        log.info("Signature Step 5.1: Razorpay webhook signature is VALID");
        log.info("Signature Step 5.2: Webhook request is authenticated successfully");
        log.info("--------------------------------------------------");
        log.info("RAZORPAY WEBHOOK SIGNATURE VERIFICATION SUCCESSFUL");
        log.info("--------------------------------------------------");
    }

    /**
     * ==========================================================
     * PROCESS WEBHOOK BUSINESS LOGIC
     * ==========================================================
     */
//    @Transactional
//    public void processWebhookBusinessLogic(
//            String eventType,
//            List<PaymentDetailsV2> payments,
//            JsonNode paymentEntity,
//            JsonNode refundEntity) {
//
//        switch (eventType) {
//
//            case "payment.captured":
//                handlePaymentCaptured(payments, paymentEntity);
//                break;
//
//            case "payment.failed":
//                handlePaymentFailed(payments, paymentEntity);
//                break;
//
//            case "order.paid":
//                log.info(
//                        "order.paid received. " +
//                                "Payment status handled by payment.captured."
//                );
//                break;
//
//            case "refund.created":
//                handleRefundCreated(refundEntity);
//                break;
//
//            case "refund.processed":
//                handleRefundProcessed(refundEntity);
//                break;
//
//            default:
//                log.info(
//                        "Unhandled Razorpay event type: {}",
//                        eventType
//                );
//        }
//    }

    /**
     * ==========================================================
     * HANDLE PAYMENT CAPTURED
     * ==========================================================
     *
     * PENDING -> PAID
     */
//    private void handlePaymentCaptured(
//            Optional<PaymentDetailsV2> paymentOpt,
//            JsonNode paymentEntity) {
//
//        String gatewayOrderId =
//                textOrNull(
//                        paymentEntity,
//                        "order_id"
//                );
//
//
//
//
//        PaymentDetailsV2 payment =
//                paymentOpt.orElseThrow(
//                        () -> new IllegalStateException(
//                                "payment.captured received "
//                                        + "for unknown order: "
//                                        + gatewayOrderId
//                        )
//                );
//
//
//        /*
//         * ======================================================
//         * GET PAID STATUS
//         * ======================================================
//         */
//        Long paidStatusId =
//                paymentUtils
//                        .getPaymentStatus(
//                                PaymentStatusCode.PAID
//                        )
//                        .getId();
//
//
//        /*
//         * ======================================================
//         * GET RAZORPAY PAYMENT ID
//         * ======================================================
//         */
//        String gatewayPaymentId =
//                textOrNull(
//                        paymentEntity,
//                        "id"
//                );
//        String methodRaw = textOrNull(paymentEntity, "method");
//        payment.setPaymentModeId(paymentUtils.resolvePaymentModeId(methodRaw));
//
//        String paymentVia = extractPaymentVia(paymentEntity);
//        payment.setPaymentVia(paymentVia);
//
//
//
//        /*
//         * ======================================================
//         * UPDATE PAYMENT STATUS
//         * ======================================================
//         */
//        payment.setPaymentStatusId(
//                paidStatusId
//        );
//
//
//        /*
//         * Razorpay payment ID.
//         */
//        if (gatewayPaymentId != null) {
//
//            payment.setGatewayPaymentId(
//                    gatewayPaymentId
//            );
//        }
//
//
//        /*
//         * Razorpay payment status.
//         */
//        payment.setGatewayPaymentStatus(
//                "captured"
//        );
//
//
//        /*
//         * Payment date.
//         *
//         * Only set if not already available.
//         */
//        if (payment.getPaymentDate() == null) {
//
//            payment.setPaymentDate(
//                    HMISUtil.getCurrentLocalDateTime()
//            );
//        }
//
//
//        /*
//         * ======================================================
//         * GENERATE RECEIPT
//         * ======================================================
//         *
//         * Only generate a receipt if one doesn't already exist.
//         */
//        if (payment.getReceiptNo() == null
//                || payment.getReceiptNo().isBlank()) {
//
//            payment.setReceiptNo(
//                    paymentUtils.generateReceiptNumber(payment.getBillingHeader())
//            );
//        }
//
//
//        /*
//         * ======================================================
//         * SAVE PAYMENT
//         * ======================================================
//         */
//        paymentRepository.save(
//                payment
//        );
//
//
//        log.info(
//                "Payment marked PAID. " +
//                        "paymentId={}, orderId={}, " +
//                        "gatewayPaymentId={}",
//                payment.getPaymentId(),
//                payment.getGatewayOrderId(),
//                gatewayPaymentId
//        );
//    }
    private void handlePaymentCaptured(
            List<PaymentDetailsV2> payments,
            JsonNode paymentEntity) {

        String gatewayOrderId =
                textOrNull(
                        paymentEntity,
                        "order_id"
                );

        if (payments.isEmpty()) {

            throw new IllegalStateException(
                    "payment.captured received "
                            + "for unknown order: "
                            + gatewayOrderId
            );
        }


        /*
         * ======================================================
         * GET PAID STATUS
         * ======================================================
         */
        Long paidStatusId =
                paymentUtils
                        .getPaymentStatus(
                                PaymentStatusCode.PAID
                        )
                        .getId();


        /*
         * ======================================================
         * GET RAZORPAY PAYMENT ID / METHOD / VIA
         * ======================================================
         *
         * Same for every row in this group -- computed once,
         * applied to each billing header's row below.
         */
        String gatewayPaymentId =
                textOrNull(
                        paymentEntity,
                        "id"
                );
        String methodRaw = textOrNull(paymentEntity, "method");
        Long paymentModeId = paymentUtils.resolvePaymentModeId(methodRaw);
        String paymentVia = extractPaymentVia(paymentEntity);


        for (PaymentDetailsV2 payment : payments) {

            payment.setPaymentModeId(paymentModeId);
            payment.setPaymentVia(paymentVia);


            /*
             * ======================================================
             * UPDATE PAYMENT STATUS
             * ======================================================
             */
            payment.setPaymentStatusId(
                    paidStatusId
            );


            /*
             * Razorpay payment ID.
             */
            if (gatewayPaymentId != null) {

                payment.setGatewayPaymentId(
                        gatewayPaymentId
                );
            }


            /*
             * Razorpay payment status.
             */
            payment.setGatewayPaymentStatus(
                    "captured"
            );


            /*
             * Payment date.
             *
             * Only set if not already available.
             */
            if (payment.getPaymentDate() == null) {

                payment.setPaymentDate(
                        HMISUtil.getCurrentLocalDateTime()
                );
            }


            /*
             * ======================================================
             * GENERATE RECEIPT
             * ======================================================
             *
             * Only generate a receipt if one doesn't already exist.
             * Each billing header gets its OWN receipt number, since
             * each row is its own billing header's payment record.
             */
            if (payment.getReceiptNo() == null
                    || payment.getReceiptNo().isBlank()) {

                payment.setReceiptNo(
                        paymentUtils.generateReceiptNumber(payment.getBillingHeader())
                );
            }


            /*
             * ======================================================
             * SAVE PAYMENT
             * ======================================================
             */
            paymentRepository.save(
                    payment
            );


            log.info(
                    "Payment marked PAID. " +
                            "paymentId={}, billingHdId={}, orderId={}, " +
                            "gatewayPaymentId={}",
                    payment.getPaymentId(),
                    payment.getBillingHeader().getId(),
                    gatewayOrderId,
                    gatewayPaymentId
            );
        }
    }

    /**
     * ==========================================================
     * HANDLE PAYMENT FAILED
     * ==========================================================
     *
     * PENDING -> FAILED
     */
    private void handlePaymentFailed(
            List<PaymentDetailsV2> payments,
            JsonNode paymentEntity) {

        String gatewayOrderId =
                textOrNull(
                        paymentEntity,
                        "order_id"
                );

        if (payments.isEmpty()) {

            throw new IllegalStateException(
                    "payment.failed received "
                            + "for unknown order: "
                            + gatewayOrderId
            );
        }


        /*
         * ======================================================
         * GET STATUS IDS
         * ======================================================
         */
        Long paidStatusId =
                paymentUtils
                        .getPaymentStatus(
                                PaymentStatusCode.PAID
                        )
                        .getId();

        Long refundedStatusId =
                paymentUtils
                        .getPaymentStatus(
                                PaymentStatusCode.REFUNDED
                        )
                        .getId();

        Long failedStatusId =
                paymentUtils
                        .getPaymentStatus(
                                PaymentStatusCode.FAILED
                        )
                        .getId();

        String gatewayPaymentId =
                textOrNull(
                        paymentEntity,
                        "id"
                );


        for (PaymentDetailsV2 payment : payments) {

            /*
             * ======================================================
             * DO NOT DOWNGRADE FINAL SUCCESSFUL STATUS
             * ======================================================
             *
             * PAID -> FAILED     NOT ALLOWED
             * REFUNDED -> FAILED NOT ALLOWED
             *
             * Checked PER ROW -- in a multi-item order it's entirely
             * possible (though unusual) for one billing header's row
             * to already be in a terminal state while others aren't,
             * so each row is evaluated independently rather than
             * bailing out of the whole group on the first match.
             */
            if (paidStatusId.equals(
                    payment.getPaymentStatusId())) {

                log.warn(
                        "Ignoring payment.failed for already "
                                + "PAID payment. paymentId={}",
                        payment.getPaymentId()
                );

                continue;
            }


            if (refundedStatusId.equals(
                    payment.getPaymentStatusId())) {

                log.warn(
                        "Ignoring payment.failed for already "
                                + "REFUNDED payment. paymentId={}",
                        payment.getPaymentId()
                );

                continue;
            }


            /*
             * ======================================================
             * UPDATE PAYMENT STATUS
             * ======================================================
             */
            payment.setPaymentStatusId(
                    failedStatusId
            );


            /*
             * Razorpay payment status.
             */
            payment.setGatewayPaymentStatus(
                    "failed"
            );


            /*
             * Sometimes Razorpay sends payment ID in
             * payment.failed event.
             */
            if (payment.getGatewayPaymentId() == null) {

                payment.setGatewayPaymentId(
                        gatewayPaymentId
                );
            }


            /*
             * ======================================================
             * SAVE PAYMENT
             * ======================================================
             */
            paymentRepository.save(
                    payment
            );


            log.info(
                    "Payment marked FAILED. " +
                            "paymentId={}, billingHdId={}, orderId={}, " +
                            "gatewayPaymentId={}",
                    payment.getPaymentId(),
                    payment.getBillingHeader().getId(),
                    gatewayOrderId,
                    payment.getGatewayPaymentId()
            );
        }
    }


    /**
     * ==========================================================
     * HANDLE REFUND CREATED
     * ==========================================================
     *
     * REFUND_PENDING remains REFUND_PENDING.
     *
     * refund.created means Razorpay has accepted/created
     * the refund.
     *
     * Final status should be updated on refund.processed.
     */
    private void handleRefundCreated(
            JsonNode refundEntity) {

        String gatewayRefundId =
                textOrNull(
                        refundEntity,
                        "id"
                );


        if (gatewayRefundId == null) {

            log.warn(
                    "refund.created received without refund id"
            );

            return;
        }


        refundRepository
                .findByGatewayRefundId(
                        gatewayRefundId
                )
                .ifPresentOrElse(

                        refund -> {

                            log.info(
                                    "refund.created acknowledged. " +
                                            "refundId={}, gatewayRefundId={}",
                                    refund.getRefundId(),
                                    gatewayRefundId
                            );
                        },

                        () -> {

                            log.warn(
                                    "refund.created received for " +
                                            "unknown gatewayRefundId={}",
                                    gatewayRefundId
                            );
                        }
                );
    }


    /**
     * ==========================================================
     * HANDLE REFUND PROCESSED
     * ==========================================================
     *
     * REFUND_PENDING -> REFUNDED
     *
     * payment_details_v2:
     *
     * PAID -> REFUNDED
     */
    private void handleRefundProcessed(JsonNode refundEntity) {

        String gatewayRefundId =
                textOrNull(
                        refundEntity,
                        "id"
                );


        if (gatewayRefundId == null) {

            throw new IllegalStateException(
                    "refund.processed received " +
                            "without gateway refund id"
            );
        }


        /*
         * Find our refund record.
         */
        PaymentRefund refund =
                refundRepository
                        .findByGatewayRefundId(
                                gatewayRefundId
                        )
                        .orElseThrow(
                                () -> new IllegalStateException(
                                        "refund.processed received " +
                                                "for unknown gateway_refund_id: "
                                                + gatewayRefundId
                                )
                        );


        /*
         * Get REFUNDED status.
         */
        Long refundedStatusId =
                paymentUtils
                        .getPaymentStatus(
                                PaymentStatusCode.REFUNDED
                        )
                        .getId();


        /*
         * Update refund status only if not already refunded.
         */
        if (!refundedStatusId.equals(
                refund.getRefundStatusId())) {

            refund.setRefundStatusId(
                    refundedStatusId
            );


            refund.setRefundProcessedAt(
                    HMISUtil.getCurrentLocalDateTime()
            );


            refund.setUpdatedBy(
                    "SYSTEM_WEBHOOK"
            );


            refund.setUpdatedAt(
                    HMISUtil.getCurrentLocalDateTime()
            );

           //   capture RRN/ARN/UTR if the gateway has sent it by now
            setRefundReference(refund, refundEntity);


            refundRepository.save(
                    refund
            );
        }


        /*
         * Get original payment.
         */
        PaymentDetailsV2 payment =
                refund.getPayment();


        if (payment == null) {

            throw new IllegalStateException(
                    "Refund has no associated payment. " +
                            "refundId="
                            + refund.getRefundId()
            );
        }


        /*
         * Update original payment.
         */
        payment.setPaymentStatusId(
                refundedStatusId
        );


        paymentRepository.save(
                payment
        );


        log.info(
                "Refund processed successfully. " +
                        "refundId={}, paymentId={}, gatewayRefundId={}",
                refund.getRefundId(),
                payment.getPaymentId(),
                gatewayRefundId
        );
    }


    /**
     * ==========================================================
     * RESOLVE PAYMENT
     * ==========================================================
     *
     * First:
     *
     * gateway_order_id
     *
     * Then:
     *
     * gateway_payment_id
     */
//    public Optional<PaymentDetailsV2> resolvePayment(
//            String gatewayOrderId,
//            String gatewayPaymentId) {
//
//        /*
//         * Search by order ID first.
//         */
//        if (gatewayOrderId != null
//                && !gatewayOrderId.isBlank()) {
//
//            Optional<PaymentDetailsV2> paymentByOrder =
//                    paymentRepository.findByGatewayOrderId(
//                            gatewayOrderId
//                    );
//
//
//            if (paymentByOrder.isPresent()) {
//
//                return paymentByOrder;
//            }
//        }
//
//
//        /*
//         * Search by payment ID.
//         */
//        if (gatewayPaymentId != null
//                && !gatewayPaymentId.isBlank()) {
//
//            return paymentRepository.findByGatewayPaymentId(
//                    gatewayPaymentId
//            );
//        }
//
//
//        return Optional.empty();
//    }
    public List<PaymentDetailsV2> resolvePayment(
            String gatewayOrderId,
            String gatewayPaymentId) {

        /*
         * Search by order ID first — this is the group key now.
         * One order can map to MULTIPLE PaymentDetailsV2 rows
         * (one per billing header paid together in a single checkout).
         */
        if (gatewayOrderId != null && !gatewayOrderId.isBlank()) {

            List<PaymentDetailsV2> byOrder =
                    paymentRepository.findAllByGatewayOrderId(gatewayOrderId);

            if (!byOrder.isEmpty()) {
                return byOrder;
            }
        }

        /*
         * Fallback: search by gateway payment ID (used once captured,
         * e.g. for refund webhooks that only carry payment_id).
         * Still returns every row sharing that payment ID.
         */
        if (gatewayPaymentId != null && !gatewayPaymentId.isBlank()) {
            return paymentRepository.findAllByGatewayPaymentId(gatewayPaymentId);
        }

        return List.of();
    }

    private List<PaymentDetailsV2> resolveAuditPayments(
            JsonNode refundEntity, List<PaymentDetailsV2> payments) {

        if (refundEntity != null && !refundEntity.isMissingNode()) {
            String gatewayRefundId = textOrNull(refundEntity, "id");
            if (gatewayRefundId != null) {
                Optional<PaymentDetailsV2> viaRefund =
                        refundRepository.findByGatewayRefundId(gatewayRefundId)
                                .map(PaymentRefund::getPayment);
                if (viaRefund.isPresent()) {
                    return List.of(viaRefund.get());
                }
            }
        }
        return payments;
    }


    private void setRefundReference(PaymentRefund refund, JsonNode refundEntity) {

        JsonNode acquirerData = refundEntity.get("acquirer_data");

        if (acquirerData == null || acquirerData.isNull()) {
            return;
        }

        String rrn = textOrNull(acquirerData, "rrn");
        if (rrn != null) {
            refund.setGatewayReferenceNo(rrn);
            refund.setGatewayReferenceType("RRN");
            return;
        }

        String arn = textOrNull(acquirerData, "arn");
        if (arn != null) {
            refund.setGatewayReferenceNo(arn);
            refund.setGatewayReferenceType("ARN");
            return;
        }

        String utr = textOrNull(acquirerData, "utr");
        if (utr != null) {
            refund.setGatewayReferenceNo(utr);
            refund.setGatewayReferenceType("UTR");
        }
    }






    /**
     * ==========================================================
     * JSON FIELD HELPER
     * ==========================================================
     */
    private String textOrNull(JsonNode node, String field) {

        if (node == null
                || node.isMissingNode()
                || node.isNull()
                || !node.hasNonNull(field)) {

            return null;
        }

        String value = node.get(field).asText();

        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }


    /**
     * Derives a human-readable "Payment Via" string from a payment entity node,
     * based on its "method" field — UPI VPA, masked card, bank code, wallet name, etc.
     */
    private String extractPaymentVia(JsonNode paymentEntity) {

        if (paymentEntity == null
                || paymentEntity.isMissingNode()
                || paymentEntity.isNull()) {

            return null;
        }

        String method = textOrNull(paymentEntity, "method");

        if (method == null) {
            return null;
        }

        switch (method) {

            case "upi":
                return textOrNull(paymentEntity, "vpa");

            case "card": {
                JsonNode card = paymentEntity.get("card");

                if (card == null || card.isNull()) {
                    return "Card";
                }

                String network = textOrNull(card, "network"); // Visa, Mastercard, RuPay...
                String type = textOrNull(card, "type");        // credit / debit
                String last4 = textOrNull(card, "last4");

                StringBuilder label = new StringBuilder();

                if (network != null) {
                    label.append(network).append(" ");
                }

                if (type != null) {
                    label.append(Character.toUpperCase(type.charAt(0)))
                            .append(type.substring(1))
                            .append(" Card");
                } else {
                    label.append("Card");
                }

                if (last4 != null) {
                    label.append(" \u2022\u2022\u2022\u2022 ").append(last4);
                }

                return label.toString().trim();
            }

            case "netbanking":
                return textOrNull(paymentEntity, "bank");

            case "wallet":
                return textOrNull(paymentEntity, "wallet");

            case "emi": {
                JsonNode card = paymentEntity.get("card");
                String last4 = card != null ? textOrNull(card, "last4") : null;
                return last4 != null ? "EMI \u2022\u2022\u2022\u2022 " + last4 : "EMI";
            }

            case "paylater": {
                String provider = textOrNull(paymentEntity, "provider");
                return provider != null ? provider : "Pay Later";
            }

            default:
                return method;
        }
    }


    /**
     * ==========================================================
     * FIRST NON-NULL VALUE
     * ==========================================================
     */
    private String firstNonNull(String first, String second) {
        return first != null ? first : second;
    }
}
