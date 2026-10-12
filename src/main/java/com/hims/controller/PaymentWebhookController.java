package com.hims.controller;

import com.hims.service.RazorpayWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint you register in Razorpay Dashboard -> Settings -> Webhooks.
 * URL: https://<your-domain>/api/payments/webhook
 * Events to enable at minimum: payment.captured, payment.failed, order.paid,
 * refund.created, refund.processed.
 *
 * IMPORTANT: this endpoint must be excluded from your Spring Security JWT/auth
 * filter chain (Razorpay's servers call this directly, with no HMIS session/token -
 * only the webhook signature proves authenticity). Add a permitAll() rule for
 * "/api/payments/webhook" in your SecurityConfig.
 *
 * IMPORTANT: @RequestBody String here captures the raw body exactly as sent.
 * Do NOT change this to a parsed DTO/JsonNode parameter - Razorpay's signature
 * is computed over the exact raw bytes, and re-serializing a parsed object
 * before verification will make every signature check fail.
 */
@Slf4j
@RestController
@RequestMapping("/api/payment/razorpay")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final RazorpayWebhookService webhookService;

    @Value("${razorpay.webhook.secret}")
    private String webhookSecret;


    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader("X-Razorpay-Signature") String signature,
            @RequestHeader(value = "X-Razorpay-Event-Id", required = false) String eventIdHeader) {

        log.info("========== RAZORPAY WEBHOOK STARTED ==========");

        log.info("Changed Controller");

        log.info("Step 1: Webhook endpoint received request");

        log.info("Step 2: Request signature received: {}", signature);

        log.info("Step 3: Raw request body received: {}", rawBody);

        log.info("Step 3.1:event id header  received: {}", eventIdHeader);

        try {

            log.info("Step 4: Starting webhook signature validation and processing");

            log.info("Step 5: Calling webhookService.processWebhook()");

            webhookService.processWebhook(
                    rawBody,
                    signature,
                    eventIdHeader,
                    webhookSecret
            );

            log.info("Step 6: webhookService.processWebhook() completed successfully");

            log.info("Step 7: Webhook accepted successfully");

            log.info("Step 8: Returning HTTP 200 OK to Razorpay");

            log.info("========== RAZORPAY WEBHOOK COMPLETED SUCCESSFULLY ==========");

            return ResponseEntity.ok("OK");

        } catch (SecurityException e) {

            log.warn("Step ERROR: Webhook signature validation failed");

            log.warn("Step ERROR: Reason: {}", e.getMessage());

            log.warn("Step ERROR: Razorpay webhook rejected because signature is invalid");

            log.warn("Step ERROR: Returning HTTP 400 Bad Request");

            log.warn("========== RAZORPAY WEBHOOK REJECTED ==========");

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid signature");

        } catch (Exception e) {

            log.error("Step ERROR: Unexpected exception occurred while processing webhook");

            log.error("Step ERROR: Exception type: {}", e.getClass().getName());

            log.error("Step ERROR: Exception message: {}", e.getMessage());

            log.error("Step ERROR: Full exception stack trace", e);

            log.error("Step ERROR: Returning HTTP 500 Internal Server Error");

            log.error("========== RAZORPAY WEBHOOK PROCESSING FAILED ==========");

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Processing error");
        }
    }
}