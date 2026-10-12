package com.hims.controller;


import com.hims.entity.PaymentRefund;
import com.hims.request.OrderRequest;
import com.hims.request.RefundRequest;
import com.hims.request.VerifyRequest;
import com.hims.response.PaymentGatewayStatusResponse;
import com.hims.service.PatientService;
import com.hims.service.PaymentGatewayService;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentGatewayService paymentGatewayService;

    private final PatientService patientService;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    /**
     * Creates a Razorpay order AND a PENDING row in payment_details_v2 in one step.
     * The frontend's `verify` call after checkout is now just a UX confidence-check -
     * payment_status_id only ever moves to PAID/FAILED via the payment.captured /
     * payment.failed webhook, handled by PaymentWebhookController. This makes the
     * webhook the single source of truth, immune to the user closing the tab early.
     */
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest request) throws RazorpayException {
        log.info("Creating Razorpay order controller called...");
        Map<String, Object> response = paymentGatewayService.createPaymentOrder(request);
        log.info("Creating Razorpay order controller ended...");
        return ResponseEntity.ok(response);
    }

    /**
     * Initiates a refund for a cancelled/refunded bill. Sets REFUND_PENDING and calls
     * Razorpay's refund API; the row only becomes REFUNDED once refund.processed
     * arrives at the webhook endpoint.
     */
    @PostMapping("/refund")
    public ResponseEntity<?> initiateRefund(
            @Valid @RequestBody RefundRequest request)
            throws RazorpayException {

        log.info("==================================================");
        log.info("RAZORPAY REFUND REQUEST STARTED");
        log.info("==================================================");

        log.info("Step 1: Refund API request received");

        log.info(
                "Step 1.1: Refund request object received successfully",request
        );

        /*
         * ======================================================
         * 2. INITIATE REFUND
         * ======================================================
         */

        log.info(
                "Step 2: Starting refund initiation"
        );

            log.info(
                    "Step 2.1: Calling paymentGatewayService.initiateRefund()"
            );

            PaymentRefund refund =
                    paymentGatewayService.initiateRefund(request);

            log.info(
                    "Step 2.2: Refund service completed successfully"
            );

            log.info(
                    "Step 2.3: Internal refundId: {}",
                    refund.getRefundId()
            );

            log.info(
                    "Step 2.4: Refund reference number: {}",
                    refund.getRefundReferenceNo()
            );

            log.info(
                    "Step 2.5: Gateway refund ID: {}",
                    refund.getGatewayRefundId()
            );


            /*
             * ==================================================
             * 3. BUILD RESPONSE
             * ==================================================
             */

            log.info(
                    "Step 3: Building refund API response"
            );

            Map<String, Object> response = Map.of(
                    "refundId", refund.getRefundId(),
                    "refundReferenceNo", refund.getRefundReferenceNo(),
                    "gatewayRefundId", refund.getGatewayRefundId(),
                    "status", "REFUND_PENDING"
            );

            log.info(
                    "Step 3.1: Refund response created successfully"
            );

            log.info(
                    "Step 3.2: Refund status: REFUND_PENDING"
            );


            /*
             * ==================================================
             * 4. SUCCESS RESPONSE
             * ==================================================
             */

            log.info(
                    "Step 4: Returning HTTP 200 OK response"
            );

            log.info(
                    "=================================================="
            );

            log.info(
                    "RAZORPAY REFUND REQUEST COMPLETED SUCCESSFULLY"
            );

            log.info(
                    "refundId={}, refundReferenceNo={}, gatewayRefundId={}",
                    refund.getRefundId(),
                    refund.getRefundReferenceNo(),
                    refund.getGatewayRefundId()
            );

            log.info(
                    "=================================================="
            );

            return ResponseEntity.ok(response);


    }

    /**
     * NOTE - this is a UX confidence-check only, not the source of truth.
     * It lets the frontend show "Payment successful" immediately after checkout
     * closes, without waiting for the webhook round-trip. It does NOT update
     * payment_status_id - only the payment.captured/payment.failed webhook does
     * that (see RazorpayWebhookService), so the DB stays correct even if the
     * user closes the tab right after this call or the call never happens.
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody VerifyRequest request) {
        try {
            log.info("verifyPayment controller called...");
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", request.getRazorpayOrderId());
            options.put("razorpay_payment_id", request.getRazorpayPaymentId());
            options.put("razorpay_signature", request.getRazorpaySignature());

            boolean isValid = Utils.verifyPaymentSignature(options, keySecret);

            log.info("verifyPayment controller ended...");

            return isValid
                    ? ResponseEntity.ok(Map.of("status", "success"))
                    : ResponseEntity.status(400).body(Map.of("status", "verification_failed"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }

    @GetMapping("/status/{paymentId}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable Long paymentId) {

        log.info("getPaymentStatus controller called...");
        PaymentGatewayStatusResponse paymentStatus = paymentGatewayService.getPaymentStatus(paymentId);
        log.info("getPaymentStatus controller ended...");

        return ResponseEntity.ok(
                paymentStatus
        );
    }


    @GetMapping("/razorpay-prefill/{patientId}")
    public ResponseEntity<?> getRazorpayPrefill(
            @PathVariable Long patientId) {

        return ResponseEntity.ok(patientService.getRazorpayPrefillDetails(patientId));
    }
}