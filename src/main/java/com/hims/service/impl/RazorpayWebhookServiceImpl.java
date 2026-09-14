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

        log.info("==================================================");
        log.info("RAZORPAY WEBHOOK SERVICE PROCESSING STARTED");
        log.info("==================================================");

        log.info("Step 1: processWebhook() method entered");
        log.info("Step 1.1: Razorpay signature received: {}", razorpaySignature);
        log.info("Step 1.1.1: Event id header received: {}", eventIdHeader);
        log.info("Step 1.2: Webhook secret configured: {}", webhookSecret != null && !webhookSecret.isBlank());
        log.info("Step 1.3: Raw webhook body received. Payload length: {}", rawBody != null ? rawBody.length() : 0);


        /*
         * ======================================================
         * 1. VERIFY RAZORPAY SIGNATURE
         * ======================================================
         */

        log.info("Step 2: Starting Razorpay webhook signature verification");

        try {
            verifyWebhookSignature(rawBody, razorpaySignature, webhookSecret);
            log.info("Step 2.1: Razorpay webhook signature verified successfully");
        } catch (Exception e) {
            log.error("Step 2.2: Razorpay webhook signature verification FAILED", e);
            throw e;
        }


        /*
         * ======================================================
         * 2. PARSE WEBHOOK JSON
         * ======================================================
         */

        log.info("Step 3: Starting webhook JSON parsing");
        JsonNode root;

        try {

            root = objectMapper.readTree(rawBody);
            log.info("Step 3.1: Webhook JSON parsed successfully");

        } catch (Exception e) {

            log.error("Step 3.2: Unable to parse Razorpay webhook JSON", e);
            throw new IllegalArgumentException("Malformed webhook JSON body", e);
        }


        /*
         * ======================================================
         * 3. GET EVENT ID AND EVENT TYPE
         * ======================================================
         */

        log.info("Step 4: Extracting event ID and event type from webhook");
        String eventId = eventIdHeader;
        String eventType = textOrNull(root, "event");

        log.info("Step 4.1: Extracted eventId={}", eventId);
        log.info("Step 4.2: Extracted eventType={}", eventType);

        if (eventId == null || eventId.isBlank() || eventType == null) {
            log.error("Step 4.3: Webhook payload is missing required event id (header) or 'event' field");
            throw new IllegalArgumentException(
                    "Webhook missing X-Razorpay-Event-Id header or 'event' field"
            );
        }

        log.info(
                "Step 4.4: Event ID and event type validation successful. " +
                        "eventId={}, eventType={}",
                eventId,
                eventType
        );


        /*
         * ======================================================
         * 4. IDEMPOTENCY CHECK
         * ======================================================
         */

        log.info(
                "Step 5: Checking whether webhook event already exists. " +
                        "gateway={}, eventId={}",
                RAZORPAY,
                eventId
        );

        boolean eventAlreadyExists =
                webhookEventRepository
                        .existsByGatewayAndEventId(
                                RAZORPAY,
                                eventId
                        );

        log.info(
                "Step 5.1: Idempotency check completed. eventAlreadyExists={}",
                eventAlreadyExists
        );


        if (eventAlreadyExists) {

            log.info(
                    "Step 5.2: Razorpay webhook already exists. " +
                            "eventId={}, eventType={}",
                    eventId,
                    eventType
            );

            log.info(
                    "Step 5.3: Skipping duplicate webhook processing"
            );

            log.info(
                    "========== RAZORPAY DUPLICATE WEBHOOK COMPLETED =========="
            );

            return;
        }

        log.info(
                "Step 5.4: Webhook is new. Continuing processing"
        );


        /*
         * ======================================================
         * 5. GET PAYMENT / ORDER / REFUND ENTITY
         * ======================================================
         */

        log.info("Step 6: Extracting payment, order and refund entities");

        JsonNode paymentEntity =
                root.at("/payload/payment/entity");

        JsonNode orderEntity =
                root.at("/payload/order/entity");

        JsonNode refundEntity =
                root.at("/payload/refund/entity");

        log.info(
                "Step 6.1: Payment entity present: {}",
                !paymentEntity.isMissingNode() && !paymentEntity.isNull()
        );

        log.info(
                "Step 6.2: Order entity present: {}",
                !orderEntity.isMissingNode() && !orderEntity.isNull()
        );

        log.info(
                "Step 6.3: Refund entity present: {}",
                !refundEntity.isMissingNode() && !refundEntity.isNull()
        );


        /*
         * ======================================================
         * 6. GET GATEWAY ORDER ID
         * ======================================================
         */

        log.info("Step 7: Extracting Razorpay gateway order ID");

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

        log.info(
                "Step 7.1: Gateway order ID resolved: {}",
                gatewayOrderId
        );


        /*
         * ======================================================
         * 7. GET GATEWAY PAYMENT ID
         * ======================================================
         */

        log.info("Step 8: Extracting Razorpay gateway payment ID");

        String gatewayPaymentId = firstNonNull(
                                        textOrNull(paymentEntity, "id"),
                                        textOrNull(refundEntity, "payment_id")
                                  );

        log.info("Step 8.1: Gateway payment ID resolved: {}", gatewayPaymentId);


        /*
         * ======================================================
         * 8. FIND HMIS PAYMENT
         * ======================================================
         */

        log.info("Step 9: Resolving HMIS payment using gateway identifiers");
        log.info("Step 9.1: Searching payment using gatewayOrderId={}", gatewayOrderId);
        log.info("Step 9.2: Searching payment using gatewayPaymentId={}", gatewayPaymentId);

        Optional<PaymentDetailsV2> paymentOpt =
                resolvePayment(
                        gatewayOrderId,
                        gatewayPaymentId
                );

        log.info("Step 9.3: HMIS payment lookup completed. Payment found={}", paymentOpt.isPresent());

        if (paymentOpt.isPresent()) {
            log.info("Step 9.4: HMIS payment successfully resolved");

        } else {
            log.warn("Step 9.4: HMIS payment NOT found. " +
                            "gatewayOrderId={}, gatewayPaymentId={}",
                    gatewayOrderId,
                    gatewayPaymentId
            );
        }


        /*
         * ======================================================
         * 9. SAVE WEBHOOK EVENT
         * ======================================================
         */

        log.info(
                "Step 10: Saving webhook event into database"
        );

        log.info(
                "Step 10.1: Calling webhookAuditService.saveWebhookEvent()"
        );

        PaymentWebhookEvent webhookEvent =
                webhookAuditService.saveWebhookEvent(
                        eventId,
                        eventType,
                        gatewayOrderId,
                        gatewayPaymentId,
                        rawBody,
                        paymentOpt
                );

        log.info(
                "Step 10.2: Webhook event saved successfully. " +
                        "webhookEventId={}",
                webhookEvent.getWebhookEventId()
        );


        /*
         * ======================================================
         * 10. PROCESS BUSINESS LOGIC
         * ======================================================
         */

        log.info(
                "Step 11: Starting webhook business logic processing"
        );

        log.info(
                "Step 11.1: Calling processWebhookBusinessLogic(). " +
                        "eventType={}, eventId={}",
                eventType,
                eventId
        );

        try {

            processWebhookBusinessLogic(
                    eventType,
                    paymentOpt,
                    paymentEntity,
                    refundEntity
            );

            log.info(
                    "Step 11.2: Webhook business logic completed successfully"
            );


            /*
             * ==================================================
             * 11. MARK WEBHOOK PROCESSED
             * ==================================================
             */

            log.info(
                    "Step 12: Marking webhook event as PROCESSED"
            );

            log.info(
                    "Step 12.1: webhookEventId={}",
                    webhookEvent.getWebhookEventId()
            );

            webhookAuditService.markProcessed(
                    webhookEvent.getWebhookEventId()
            );

            log.info(
                    "Step 12.2: Webhook event marked as PROCESSED successfully"
            );


            /*
             * ==================================================
             * SUCCESS
             * ==================================================
             */

            log.info(
                    "=================================================="
            );

            log.info(
                    "RAZORPAY WEBHOOK PROCESSED SUCCESSFULLY"
            );

            log.info(
                    "eventId={}, eventType={}, " +
                            "gatewayOrderId={}, gatewayPaymentId={}, " +
                            "webhookEventId={}",
                    eventId,
                    eventType,
                    gatewayOrderId,
                    gatewayPaymentId,
                    webhookEvent.getWebhookEventId()
            );

            log.info(
                    "=================================================="
            );


        } catch (Exception e) {

            /*
             * ==================================================
             * 12. BUSINESS PROCESSING FAILED
             * ==================================================
             */

            log.error(
                    "=================================================="
            );

            log.error(
                    "Step ERROR 13: Razorpay webhook business processing FAILED"
            );

            log.error(
                    "Step ERROR 13.1: eventId={}",
                    eventId
            );

            log.error(
                    "Step ERROR 13.2: eventType={}",
                    eventType
            );

            log.error(
                    "Step ERROR 13.3: gatewayOrderId={}",
                    gatewayOrderId
            );

            log.error(
                    "Step ERROR 13.4: gatewayPaymentId={}",
                    gatewayPaymentId
            );

            log.error(
                    "Step ERROR 13.5: webhookEventId={}",
                    webhookEvent.getWebhookEventId()
            );

            log.error(
                    "Step ERROR 13.6: Exception type={}",
                    e.getClass().getName()
            );

            log.error(
                    "Step ERROR 13.7: Exception message={}",
                    e.getMessage()
            );

            log.error(
                    "Step ERROR 13.8: Full exception stack trace",
                    e
            );


            /*
             * ==================================================
             * 13. MARK WEBHOOK FAILED
             * ==================================================
             */

            log.info(
                    "Step 14: Attempting to mark webhook event as FAILED"
            );

            try {

                webhookAuditService.markFailed(
                        webhookEvent.getWebhookEventId(),
                        e.getMessage()
                );

                log.info(
                        "Step 14.1: Webhook event marked as FAILED successfully. " +
                                "webhookEventId={}",
                        webhookEvent.getWebhookEventId()
                );

            } catch (Exception auditException) {

                log.error(
                        "Step 14.2: Unable to mark webhook event as FAILED. " +
                                "webhookEventId={}",
                        webhookEvent.getWebhookEventId(),
                        auditException
                );
            }


            /*
             * ==================================================
             * FINAL FAILURE
             * ==================================================
             */

            log.error("Step 15: Business processing failure handled");
            log.error("Step 15.1: Webhook was already stored in audit table");
            log.error("Step 15.2: Webhook status should be FAILED");
            log.error("Step 15.3: Exception will NOT be re-thrown");
            log.error("==================================================");
            log.error("RAZORPAY WEBHOOK PROCESSING FINISHED WITH FAILURE");
            log.error("==================================================");
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
    @Transactional
    public void processWebhookBusinessLogic(
            String eventType,
            Optional<PaymentDetailsV2> paymentOpt,
            JsonNode paymentEntity,
            JsonNode refundEntity) {

        switch (eventType) {

            /*
             * ==================================================
             * PAYMENT CAPTURED
             * ==================================================
             */
            case "payment.captured":

                handlePaymentCaptured(
                        paymentOpt,
                        paymentEntity
                );

                break;


            /*
             * ==================================================
             * PAYMENT FAILED
             * ==================================================
             */
            case "payment.failed":

                handlePaymentFailed(
                        paymentOpt,
                        paymentEntity
                );

                break;


            /*
             * ==================================================
             * ORDER PAID
             * ==================================================
             *
             * payment.captured already updates payment status.
             */
            case "order.paid":

                log.info(
                        "order.paid received. " +
                                "Payment status handled by payment.captured."
                );

                break;


            /*
             * ==================================================
             * REFUND CREATED
             * ==================================================
             */
            case "refund.created":

                handleRefundCreated(
                        refundEntity
                );

                break;


            /*
             * ==================================================
             * REFUND PROCESSED
             * ==================================================
             */
            case "refund.processed":

                handleRefundProcessed(
                        refundEntity
                );

                break;


            /*
             * ==================================================
             * OTHER EVENTS
             * ==================================================
             */
            default:

                log.info(
                        "Unhandled Razorpay event type: {}",
                        eventType
                );
        }
    }

    /**
     * ==========================================================
     * HANDLE PAYMENT CAPTURED
     * ==========================================================
     *
     * PENDING -> PAID
     */
    private void handlePaymentCaptured(
            Optional<PaymentDetailsV2> paymentOpt,
            JsonNode paymentEntity) {

        String gatewayOrderId =
                textOrNull(
                        paymentEntity,
                        "order_id"
                );




        PaymentDetailsV2 payment =
                paymentOpt.orElseThrow(
                        () -> new IllegalStateException(
                                "payment.captured received "
                                        + "for unknown order: "
                                        + gatewayOrderId
                        )
                );


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
         * GET RAZORPAY PAYMENT ID
         * ======================================================
         */
        String gatewayPaymentId =
                textOrNull(
                        paymentEntity,
                        "id"
                );
        String methodRaw = textOrNull(paymentEntity, "method");
        payment.setPaymentModeId(paymentUtils.resolvePaymentModeId(methodRaw));

        String paymentVia = extractPaymentVia(paymentEntity);
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
                        "paymentId={}, orderId={}, " +
                        "gatewayPaymentId={}",
                payment.getPaymentId(),
                payment.getGatewayOrderId(),
                gatewayPaymentId
        );
    }


    /**
     * ==========================================================
     * HANDLE PAYMENT FAILED
     * ==========================================================
     *
     * PENDING -> FAILED
     */
    private void handlePaymentFailed(
            Optional<PaymentDetailsV2> paymentOpt,
            JsonNode paymentEntity) {

        String gatewayOrderId =
                textOrNull(
                        paymentEntity,
                        "order_id"
                );


        PaymentDetailsV2 payment =
                paymentOpt.orElseThrow(
                        () -> new IllegalStateException(
                                "payment.failed received "
                                        + "for unknown order: "
                                        + gatewayOrderId
                        )
                );


        /*
         * ======================================================
         * GET CURRENT PAYMENT STATUS
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


        /*
         * ======================================================
         * DO NOT DOWNGRADE FINAL SUCCESSFUL STATUS
         * ======================================================
         *
         * PAID -> FAILED     NOT ALLOWED
         * REFUNDED -> FAILED NOT ALLOWED
         */
        if (paidStatusId.equals(
                payment.getPaymentStatusId())) {

            log.warn(
                    "Ignoring payment.failed for already "
                            + "PAID payment. paymentId={}",
                    payment.getPaymentId()
            );

            return;
        }


        if (refundedStatusId.equals(
                payment.getPaymentStatusId())) {

            log.warn(
                    "Ignoring payment.failed for already "
                            + "REFUNDED payment. paymentId={}",
                    payment.getPaymentId()
            );

            return;
        }


        /*
         * ======================================================
         * GET FAILED STATUS
         * ======================================================
         */
        Long failedStatusId =
                paymentUtils
                        .getPaymentStatus(
                                PaymentStatusCode.FAILED
                        )
                        .getId();


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
                    textOrNull(
                            paymentEntity,
                            "id"
                    )
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
                        "paymentId={}, orderId={}, " +
                        "gatewayPaymentId={}",
                payment.getPaymentId(),
                payment.getGatewayOrderId(),
                payment.getGatewayPaymentId()
        );
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
    public Optional<PaymentDetailsV2> resolvePayment(
            String gatewayOrderId,
            String gatewayPaymentId) {

        /*
         * Search by order ID first.
         */
        if (gatewayOrderId != null
                && !gatewayOrderId.isBlank()) {

            Optional<PaymentDetailsV2> paymentByOrder =
                    paymentRepository.findByGatewayOrderId(
                            gatewayOrderId
                    );


            if (paymentByOrder.isPresent()) {

                return paymentByOrder;
            }
        }


        /*
         * Search by payment ID.
         */
        if (gatewayPaymentId != null
                && !gatewayPaymentId.isBlank()) {

            return paymentRepository.findByGatewayPaymentId(
                    gatewayPaymentId
            );
        }


        return Optional.empty();
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
